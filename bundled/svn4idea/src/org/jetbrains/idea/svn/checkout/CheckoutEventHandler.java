package org.jetbrains.idea.svn.checkout;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

public class CheckoutEventHandler implements ISVNEventHandler {
  private final ProgressIndicator myIndicator;
  private int myExternalsCount;
  private final SvnVcs myVCS;
  private final boolean myIsExport;

  public CheckoutEventHandler(SvnVcs vcs, boolean isExport, ProgressIndicator indicator) {
    myIndicator = indicator;
    myVCS = vcs;
    myExternalsCount = 1;
    myIsExport = isExport;
  }

  public void handleEvent(SVNEvent event, double progress) {
    final String path = SvnUtil.getPathForProgress(event);
    if (path == null) {
      return;
    }
    if (event.getAction() == SVNEventAction.UPDATE_EXTERNAL) {
      myExternalsCount++;
      myIndicator.setText(SvnBundle.message("progress.text2.fetching.external.location", event.getFile().getAbsolutePath()));
      myIndicator.setText2("");
    }
    else if (event.getAction() == SVNEventAction.UPDATE_ADD) {
      myIndicator.setText2(SvnBundle.message(myIsExport ? "progress.text2.exported" : "progress.text2.checked.out", event.getFile().getName()));
    }
    else if (event.getAction() == SVNEventAction.UPDATE_COMPLETED) {
      myExternalsCount--;
      myIndicator.setText2(SvnBundle.message(myIsExport ? "progress.text2.exported.revision" : "progress.text2.checked.out.revision", event.getRevision()));
      if (myExternalsCount == 0 && event.getRevision() >= 0 && myVCS != null) {
        myExternalsCount = 1;
        Project project = myVCS.getProject();
        if (project != null) {
          StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
          if (statusBar != null) {
            statusBar.setInfo(SvnBundle.message(myIsExport ? "progress.text2.exported.revision" : "status.text.checked.out.revision", event.getRevision()));
          }
        }
      }
    } else if (event.getAction() == SVNEventAction.COMMIT_ADDED) {
      myIndicator.setText2(SvnBundle.message("progress.text2.adding", path));
    } else if (event.getAction() == SVNEventAction.COMMIT_DELTA_SENT) {
      myIndicator.setText2(SvnBundle.message("progress.text2.transmitting.delta", path));
    }
  }

  public void checkCancelled() throws SVNCancelException {
    if (myIndicator.isCanceled()) {
      throw new SVNCancelException(SVNErrorMessage.create(SVNErrorCode.CANCELLED, "Operation cancelled"));
    }
  }
}
