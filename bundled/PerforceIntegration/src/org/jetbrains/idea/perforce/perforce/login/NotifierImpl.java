package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.changes.ui.ChangesViewBalloonProblemNotifier;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.PerforceLoginDialog;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import javax.swing.event.HyperlinkEvent;
import java.util.HashMap;
import java.util.Map;

public class NotifierImpl implements Notifier {
  private final Project myProject;
  private PerforceConnectionManager myConnectionManager;
  private PerforceSettings mySettings;
  private static final String PERFORCE_LOGIN_NOTIFIER = "Perforce";

  private final Map<ConnectionId, PerforceNotification> myState = new HashMap<ConnectionId, PerforceNotification>();

  private final Object myLock = new Object();
  private final PerforceManager myPerforceManager;

  public NotifierImpl(final Project project, final PerforceManager perforceManager) {
    myProject = project;
    myConnectionManager = PerforceConnectionManager.getInstance(project);
    mySettings = PerforceSettings.getSettings(project);
    myPerforceManager = perforceManager;
  }

  public void ensureNotify(final P4Connection connection) {
    if (! (mySettings.ENABLED && mySettings.USE_LOGIN)) return;

    synchronized (myLock) {
      if (myState.containsKey(connection.getId())) return;
      final String present = (connection.getId().myWorkingDir == null ? "Not Logged to Perforce. <a href=\"\">Click to fix.</a>" :
                              ("Not logged to Perforce: " + connection.getId().myWorkingDir + ". <a href=\"\">Click to fix.</a>"));

      final PerforceNotification notification =
        new PerforceNotification(PERFORCE_LOGIN_NOTIFIER, "Not Logged To Perforce", present, NotificationType.ERROR, new NotificationListener() {
          public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
            if (notification instanceof PerforceNotification) {
              final P4Connection connection1 = ((PerforceNotification)notification).getConnection();
              try {
                myPerforceManager.getLoginManager().check(connection1);
                myState.remove(connection1.getId());
                notification.expire();
              }
              catch (VcsException e) {
                //
              }
            }
          }
        }, connection);

      myState.put(connection.getId(), notification);
      Notifications.Bus.notify(notification, myProject);
    }
  }

  public void removeLazyNotification(P4Connection connection) {
    synchronized (myLock) {
      final ConnectionId ourId = connection.getId();

      final PerforceNotification notification = myState.get(ourId);
      if (notification != null) {
        myState.remove(ourId);
        notification.expire();
      }
    }
  }

  @Nullable
  public String requestForPassword(final String message, final String rootDir) {
    final String prompt;
    if (myConnectionManager.isSingletonConnectionUsed() || (rootDir == null)) {
      prompt = PerforceBundle.message("message.text.perforce.command.failed.enter.password.v2");
    } else {
      prompt = PerforceBundle.message("message.text.perforce.command.failed.withdir.enter.password.v2", rootDir);
    }
    final PerforceLoginDialog pwdDialog = new PerforceLoginDialog(myProject, prompt, mySettings.getPasswd());
    pwdDialog.show();

    final String result = pwdDialog.isOK() ? pwdDialog.getPassword() : null;
    if (result == null) {
      mySettings.disable();
      new ChangesViewBalloonProblemNotifier(myProject, "Perforce switched to offline mode", MessageType.ERROR).run();
    }
    return result;
  }

  public void showPasswordWasOk(final boolean value) {
    if (value) {
      new ChangesViewBalloonProblemNotifier(myProject, "Successfully logged into Perforce", MessageType.INFO).run();
    } else {
      new ChangesViewBalloonProblemNotifier(myProject, "Not logged into Perforce", MessageType.ERROR).run();
    }
  }

  private static class PerforceNotification extends Notification {
    private P4Connection myConnection;

    private PerforceNotification(@NotNull String groupId,
                                 @NotNull String title,
                                 @NotNull String content,
                                 @NotNull NotificationType type,
                                 @Nullable NotificationListener listener,
                                 @NotNull final P4Connection connection) {
      super(groupId, title, content, type, listener);
      myConnection = connection;
    }

    public P4Connection getConnection() {
      return myConnection;
    }
  }
}
