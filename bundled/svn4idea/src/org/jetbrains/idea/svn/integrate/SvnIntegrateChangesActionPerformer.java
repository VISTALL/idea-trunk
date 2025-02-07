package org.jetbrains.idea.svn.integrate;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnBranchConfiguration;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.actions.SelectBranchPopup;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

public class SvnIntegrateChangesActionPerformer implements SelectBranchPopup.BranchSelectedCallback {
  private final SvnVcs myVcs;
  private final MergerFactory myMergerFactory;

  private final SVNURL myCurrentBranch;

  public SvnIntegrateChangesActionPerformer(final Project project, final SVNURL currentBranchUrl, final MergerFactory mergerFactory) {
    myVcs = SvnVcs.getInstance(project);
    myCurrentBranch = currentBranchUrl;
    myMergerFactory = mergerFactory;
  }

  public void branchSelected(final Project project, final SvnBranchConfiguration configuration, final String url, final long revision) {
    onBranchSelected(url, null, null);
  }

  public void onBranchSelected(final String url, final String selectedLocalBranchPath, final String dialogTitle) {
    if (myCurrentBranch.toString().equals(url)) {
      Messages.showErrorDialog(SvnBundle.message("action.Subversion.integrate.changes.error.source.and.target.same.text"),
                               SvnBundle.message("action.Subversion.integrate.changes.messages.title"));
      return;
    }

    final Pair<WorkingCopyInfo,SVNURL> pair = IntegratedSelectedOptionsDialog.selectWorkingCopy(myVcs.getProject(), myCurrentBranch, url, true,
                                                                                                selectedLocalBranchPath, dialogTitle);
    if (pair == null) {
      return;
    }

    final WorkingCopyInfo info = pair.first;
    final SVNURL realTargetUrl = pair.second;

    final SVNURL sourceUrl = correctSourceUrl(url, realTargetUrl.toString());
    if (sourceUrl == null) {
      // should not occur
      return;
    }
    final SvnIntegrateChangesTask task = new SvnIntegrateChangesTask(myVcs, info, myMergerFactory, sourceUrl);
    ProgressManager.getInstance().run(task);
  }

  @Nullable
  private SVNURL correctSourceUrl(final String targetUrl, final String realTargetUrl) {
    try {
      if (realTargetUrl.length() > targetUrl.length()) {
        if (realTargetUrl.startsWith(targetUrl)) {
          return myCurrentBranch.appendPath(realTargetUrl.substring(targetUrl.length()), true);
        }
      } else if (realTargetUrl.equals(targetUrl)) {
        return myCurrentBranch;
      }
    }
    catch (SVNException e) {
      // tracked by return value
    }
    return null;
  }
}
