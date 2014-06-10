package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

public class LoginStateListenerImpl implements LoginStateListener {
  private ProjectLevelVcsManager myPlVcsManager;
  private PerforceVcs myVcs;
  private PerforceConnectionManager myConnectionManager;
  private final P4Connection myConnection;
  private final Runnable myListener;
  private final PerforceManager myPerforceManager;

  public LoginStateListenerImpl(final Project project, final P4Connection connection) {
    myConnection = connection;
    myPlVcsManager = ProjectLevelVcsManager.getInstance(project);
    myVcs = PerforceVcs.getInstance(project);
    myConnectionManager = PerforceConnectionManager.getInstance(project);
    myPerforceManager = PerforceManager.getInstance(project);
    myListener = new MyAfterPerforceUpdatedListener(project);
  }

  public void reconnected() {
    // todo uncomment when in change list updater - errors would be removed with respect to scope they were generated in
    /*
    final VirtualFile[] roots = myPlVcsManager.getRootsUnderVcs(myVcs);
    final List<VirtualFile> rootsToMark = new ArrayList<VirtualFile>(roots.length); // 7-8
    for (VirtualFile root : roots) {
      final P4Connection connection = myConnectionManager.getConnectionForFile(root);
      if (connection != null && myConnectionId.equals(connection.getId())) {
        rootsToMark.add(root);
      }
    }
    if (rootsToMark.isEmpty()) {
      myDirtyScopeManager.markEverythingDirty();
    } else {
      myDirtyScopeManager.filesDirty(null, rootsToMark);
    }  */
    myPerforceManager.getLoginNotifier().removeLazyNotification(myConnection);
    myPerforceManager.sendUpdateRequest(myListener);
  }

  private static class MyAfterPerforceUpdatedListener implements Runnable {
    private VcsDirtyScopeManager myDirtyScopeManager;

    private MyAfterPerforceUpdatedListener(final Project project) {
      myDirtyScopeManager = VcsDirtyScopeManager.getInstance(project);
    }

    public void run() {
      myDirtyScopeManager.markEverythingDirty();
    }
  }
}
