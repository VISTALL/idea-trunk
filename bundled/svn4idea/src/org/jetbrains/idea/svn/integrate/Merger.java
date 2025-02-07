package org.jetbrains.idea.svn.integrate;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.util.NotNullFunction;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.update.UpdateEventHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffOptions;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Merger {
  protected final List<CommittedChangeList> myChangeLists;
  protected final File myTarget;
  protected final SVNDiffClient myDiffClient;
  protected int myCount;
  private final ProgressIndicator myProgressIndicator;
  protected CommittedChangeList myLatestProcessed;
  protected final SVNURL myCurrentBranchUrl;
  private final StringBuilder myCommitMessage;
  protected final SvnConfiguration mySvnConfig;
  private final Project myProject;

  public Merger(final SvnVcs vcs, final List<CommittedChangeList> changeLists, final File target, final UpdateEventHandler handler, final SVNURL currentBranchUrl) {
    myProject = vcs.getProject();
    mySvnConfig = SvnConfiguration.getInstanceChecked(vcs.getProject());
    myCurrentBranchUrl = currentBranchUrl;
    myDiffClient = vcs.createDiffClient();
    myChangeLists = changeLists;

    Collections.sort(myChangeLists, ByNumberChangeListComparator.getInstance());

    myTarget = target;
    myCount = 0;
    myProgressIndicator = ProgressManager.getInstance().getProgressIndicator();
    myDiffClient.setEventHandler(handler);
    myDiffClient.setMergeOptions(new SVNDiffOptions(mySvnConfig.IGNORE_SPACES_IN_MERGE, mySvnConfig.IGNORE_SPACES_IN_MERGE,
                                                    mySvnConfig.IGNORE_SPACES_IN_MERGE));
    myCommitMessage = new StringBuilder();
  }

  private static class ByNumberChangeListComparator implements Comparator<CommittedChangeList>{
    private final static ByNumberChangeListComparator ourInstance = new ByNumberChangeListComparator();

    public static ByNumberChangeListComparator getInstance() {
      return ourInstance;
    }

    public int compare(final CommittedChangeList o1, final CommittedChangeList o2) {
      return (int) (o1.getNumber() - o2.getNumber());
    }
  }

  public boolean hasNext() {
    return myCount < myChangeLists.size();
  }

  public void mergeNext() throws SVNException {
    myLatestProcessed = myChangeLists.get(myCount);
    ++ myCount;

    if (myProgressIndicator != null) {
      myProgressIndicator.setText2(SvnBundle.message("action.Subversion.integrate.changes.progress.integrating.details.text",
                                                     myLatestProcessed.getNumber()));
    }
    doMerge();

    appendComment();
  }

  private void appendComment() {
    final String nextComment = myLatestProcessed.getComment();
    if (nextComment.trim().length() > 0) {
      if (myCommitMessage.length() != 0) {
        myCommitMessage.append('\n');
      }
      myCommitMessage.append(nextComment);
    }
  }

  protected SVNRevisionRange createRange() {
    return new SVNRevisionRange(SVNRevision.create(myLatestProcessed.getNumber() - 1), SVNRevision.create(myLatestProcessed.getNumber()));
  }

  protected boolean isRecordOnly() {
    return false;
  }

  protected void doMerge() throws SVNException {
    myDiffClient.doMerge(myCurrentBranchUrl, SVNRevision.UNDEFINED, Collections.singletonList(createRange()),
      myTarget, SVNDepth.INFINITY, true, true, mySvnConfig.MERGE_DRY_RUN, isRecordOnly());
  }

  @NonNls
  private List<CommittedChangeList> getTail() {
    return (myCount < myChangeLists.size()) ?
           new ArrayList<CommittedChangeList>(myChangeLists.subList(myCount, myChangeLists.size())) :
           Collections.<CommittedChangeList>emptyList();
  }

  public void getInfo(final NotNullFunction<String, Boolean> holder, final boolean getLatest) {
    if (getLatest && (myLatestProcessed != null)) {
      holder.fun(SvnBundle.message("action.Subversion.integrate.changes.warning.failed.list.text", myLatestProcessed.getNumber(),
                                   myLatestProcessed.getComment().replace('\n', '|')));
    }

    getSkipped(holder);
  }

  private void getSkipped(final NotNullFunction<String, Boolean> holder) {
    final List<CommittedChangeList> tail = getTail();
    if (! tail.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < tail.size(); i++) {
        CommittedChangeList list = tail.get(i);
        if (i != 0) {
          sb.append(',');
        }
        sb.append(list.getNumber()).append(" (").append(list.getComment().replace('\n', '|')).append(')');
      }

      holder.fun(SvnBundle.message("action.Subversion.integrate.changes.warning.skipped.lists.text", sb.toString()));
    }
  }

  public String getComment() {
    return myCommitMessage.toString();
  }

  @Nullable
  public File getMergeInfoHolder() {
    return myTarget;
  }

  protected void afterProcessing() {
    myProject.getMessageBus().syncPublisher(COMMITTED_CHANGES_MERGED_STATE).event(new ArrayList<CommittedChangeList>(myChangeLists.subList(0, myCount)));
  }

  public static final Topic<CommittedChangesMergedStateChanged> COMMITTED_CHANGES_MERGED_STATE =
    new Topic<CommittedChangesMergedStateChanged>("COMMITTED_CHANGES_MERGED_STATE", CommittedChangesMergedStateChanged.class);

  public interface CommittedChangesMergedStateChanged {
    void event(final List<CommittedChangeList> list);
  }
}
