package org.jetbrains.idea.svn.history;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.List;

public class LiveProvider implements BunchProvider {
  private final SvnLogLoader myLoader;
  private final SvnRepositoryLocation myLocation;
  private boolean myEarliestRevisionWasAccessed;
  private final long myYoungestRevision;
  private final SvnVcs myVcs;

  public LiveProvider(final SvnVcs vcs, final SvnRepositoryLocation location, final long latestRevision, final SvnLogLoader loader) {
    myVcs = vcs;
    myLoader = loader;
    myLocation = location;
    myYoungestRevision = latestRevision;
  }

  public long getEarliestRevision() {
    return -1;
  }

  public boolean isEmpty() {
    return false;
  }

  public Fragment getEarliestBunchInInterval(final long earliestRevision, final long oldestRevision, final int desirableSize,
                                             final boolean includeYoungest, final boolean includeOldest) throws SVNException {
    if ((myEarliestRevisionWasAccessed) || ((oldestRevision == myYoungestRevision) && ((! includeYoungest) || (! includeOldest)))) {
      return null;
    }
    final SVNRevision youngRevision = (earliestRevision == -1) ? SVNRevision.HEAD : SVNRevision.create(earliestRevision);

    final Ref<List<CommittedChangeList>> refToList = new Ref<List<CommittedChangeList>>();
    final Ref<SVNException> exceptionRef = new Ref<SVNException>();

    final Runnable loader = new Runnable() {
      public void run() {
        try {
          refToList.set(
              myLoader.loadInterval(youngRevision, SVNRevision.create(oldestRevision), desirableSize, includeYoungest, includeOldest));
        }
        catch (SVNException e) {
          exceptionRef.set(e);
        }
      }
    };

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      loader.run();
    } else {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
        public void run() {
          final ProgressIndicator ind = ProgressManager.getInstance().getProgressIndicator();
          if (ind != null) {
            ind.setText(SvnBundle.message("progress.live.provider.loading.revisions.details.text"));
          }
          loader.run();
        }
      }, SvnBundle.message("progress.live.provider.loading.revisions.text"), false, myVcs.getProject());
    }

    if (exceptionRef.get() != null) {
      final SVNException e = exceptionRef.get();
      if (SVNErrorCode.FS_NOT_FOUND.equals(e.getErrorMessage().getErrorCode())) {
        // occurs when target URL is deleted in repository
        // try to find latest existent revision. expensive ...
        final LatestExistentSearcher searcher = new LatestExistentSearcher(oldestRevision, myYoungestRevision, (oldestRevision != 0),
                                                                           myVcs, SVNURL.parseURIEncoded(myLocation.getURL()));
        final long existent = searcher.getLatestExistent();
        if ((existent == -1) || (existent == earliestRevision)) {
          myEarliestRevisionWasAccessed = true;
          return null;
        }
        return getEarliestBunchInInterval(existent, oldestRevision, includeYoungest ? desirableSize : (desirableSize + 1), true, includeOldest);
      }
      throw e;
    }

    final List<CommittedChangeList> list = refToList.get();
    if (list.isEmpty()) {
      myEarliestRevisionWasAccessed = (oldestRevision == 0);
      return null;
    }
    myEarliestRevisionWasAccessed = (oldestRevision == 0) && ((list.size() + ((! includeOldest) ? 1 : 0) + ((! includeYoungest) ? 1 : 0)) < desirableSize);
    return new Fragment(Origin.LIVE, list, true, true, null);
  }

  public boolean isEarliestRevisionWasAccessed() {
    return myEarliestRevisionWasAccessed;
  }
}
