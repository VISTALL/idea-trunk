package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerforceLoginManagerImpl implements PerforceLoginManager {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.login.PerforceLoginManagerImpl");
  private final Notifier myNotifier;

  private final Object myMapLock;
  private final Map<ConnectionId, AttemptsStateMachine> myState;
  private PerforceSettings mySettings;
  private PerforceManager myPerforceManager;
  private final Project myProject;

  private final MySelfMonitorReactivable mySelfMonitor;

  public PerforceLoginManagerImpl(final Project project, final PerforceManager perforceManager, final Notifier notifier) {
    myProject = project;
    myMapLock = new Object();
    myState = new HashMap<ConnectionId, AttemptsStateMachine>();

    myPerforceManager = perforceManager;
    myNotifier = notifier;
    mySettings = PerforceSettings.getSettings(project);

    mySelfMonitor = new MySelfMonitorReactivable();
  }

  private boolean loginPingAllowed() {
    return mySettings.ENABLED && mySettings.USE_LOGIN;
  }

  public long ensure(final P4Connection connection) {
    LOG.debug("ensure called");
    if (! loginPingAllowed()) return -1;

    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = machine.ensure(false);
    final long recommendedInterval = machine.recommendedLazyInterval();
    if (state.isSuccess()) {
      myNotifier.removeLazyNotification(connection);
      return recommendedInterval;
    }
    // dont show login error notification when network is down - not a problem of login
    if (state.getError() != null) return recommendedInterval;

    myNotifier.ensureNotify(connection);
    return recommendedInterval;
  }

  public boolean silentLogin(P4Connection connection) throws VcsException {
    LOG.debug("silent login called");
    if (! loginPingAllowed()) {
      LOG.debug("ping is NOT allowed");
      return false;
    }
    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = machine.ensure(true);
    if (state.isSuccess()) {
      myNotifier.removeLazyNotification(connection);
      return true;
    }

    // dont show login error notification when network is down - not a problem of login
    if (state.getError() != null) {
      throw new VcsException(state.getError());
    }
    return false;
  }

  public boolean check(final P4Connection connection) throws VcsException {
    final Application application = ApplicationManager.getApplication();
    application.assertIsDispatchThread();

    if (! loginPingAllowed()) return false;

    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = machine.ensure(false);
    if (state.isSuccess()) {
      myNotifier.removeLazyNotification(connection);
      return true;
    }
    // dont show login error notification when network is down - not a problem of login
    if (state.getError() != null) {
      throw new VcsException(state.getError());
    }

    String errMessage = null;
    while (true) {
      final String newPassword = myNotifier.requestForPassword(errMessage, connection.getId().myWorkingDir);
      if (newPassword == null) {
        return false;
      }
      final LoginState newLoginState = machine.login(newPassword);
      if (newLoginState.isSuccess()) {
        mySettings.setPasswd(newPassword);
        myNotifier.showPasswordWasOk(true);
        myNotifier.removeLazyNotification(connection);
        return true;
      }
      myNotifier.showPasswordWasOk(false);
      // dont show login error notification when network is down - not a problem of login
      if (newLoginState.getError() != null) {
        throw new VcsException(newLoginState.getError());
      }
    }
  }

  public void notLogged(final P4Connection connection) {
    final AttemptsStateMachine machine = getOrCreate(connection);
    machine.failed(false, null);
  }

  private AttemptsStateMachine getOrCreate(final P4Connection connection) {
    synchronized (myMapLock) {
      final ConnectionId id = connection.getId();
      final AttemptsStateMachine machine = myState.get(id);
      if (machine != null) return machine;

      final AttemptsStateMachineImpl newMachine = new AttemptsStateMachineImpl(new LoginPerformerImpl(myProject, connection),
                                                                               new LoginStateListenerImpl(myProject, connection));
      myState.put(id, newMachine);
      return newMachine;
    }
  }

  public void startListening() {
    mySelfMonitor.start();
  }

  public void stopListening() {
    mySelfMonitor.stop();
  }

  private class MySelfMonitorReactivable extends ReactivableSingleLooped {
    private PerforceConnectionManager myConnectionManager;

    private MySelfMonitorReactivable() {
      myConnectionManager = PerforceConnectionManager.getInstance(myProject);
    }

    protected void schedule(final long timeout, final Runnable runnable) {
      myPerforceManager.queueUpdateRequest(runnable, (int) timeout);
    }

    protected long runImpl() {
      long timeout = AttemptsStateMachineImpl.defaultLazyInterval();
      final List<P4Connection> connectionList = myConnectionManager.getAllConnections(mySettings);
      for (P4Connection connection : connectionList) {
        final long currentTimeout = ensure(connection);
        if (currentTimeout != -1) {
          timeout = Math.min(timeout, currentTimeout);
        }
      }
      return timeout;
    }

    protected long getDefaultTimeout() {
      return AttemptsStateMachineImpl.defaultLazyInterval();
    }
  }
}
