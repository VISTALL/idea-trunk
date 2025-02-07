package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Getter;

public class AttemptsStateMachineImpl implements AttemptsStateMachine {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.login.AttemptsStateMachineImpl");
  private static final long ourTimeToRelogin = 3600000;
  private static final long ourSuccessBlindInterval = 600000;
  private static final long ourCredentialsBlindInterval = 1000;
  private static final long ourNetworkBlindInterval = 600000;
  private final static long ourLazyInterval = 6000000;

  private final LoginPerformer myPerformer;
  private final LoginStateListener myLoginStateListener;

  private boolean myConnectionProblem;
  private String myErrorMessage;
  private boolean mySuccess;
  private long myRecentTime;

  private final Object myLock = new Object();        

  public AttemptsStateMachineImpl(LoginPerformer performer, LoginStateListener loginStateListener) {
    myPerformer = performer;
    myLoginStateListener = loginStateListener;
    myRecentTime = -1;
  }

  private void fillTime() {
    myRecentTime = System.currentTimeMillis();
  }

  public void successful() {
    synchronized (myLock) {
      boolean wasFailure = ! mySuccess;
      myConnectionProblem = false;
      mySuccess = true;
      fillTime();
      logSuccessOrFailure(true);
      if (wasFailure) {
        LOG.debug("reconnected");
        myLoginStateListener.reconnected();
      }
    }
  }

  public LoginState login(final String password) {
    LOG.debug("login called");
    return executeUnderLock(new Getter<LoginState>() {
      public LoginState get() {
        return myPerformer.login(password);
      }
    });
  }

  private void registerResult(final LoginState state) {
    if ((myRecentTime > 0) && (mySuccess == state.isSuccess()) && (Comparing.equal(myErrorMessage, state.getError()) &&
                                                                   (myConnectionProblem == (state.getError() != null)))) {
      if ((System.currentTimeMillis() - myRecentTime) > ourSuccessBlindInterval) {
        fillTime();
      }
      LOG.debug("register result: login state didn't changed");
      return;
    }
    fillTime();
    mySuccess = state.isSuccess();
    myErrorMessage = state.getError();
    myConnectionProblem = myErrorMessage != null;
    LOG.debug("register result: success = " + mySuccess + ", network = " + myConnectionProblem);
  }

  private LoginState checkState() {
    LOG.debug("try checkState");
    final LoginState state = myPerformer.getLoginState();
    if (state.isSuccess()) {
      LOG.debug("login state success");
      final long timeLeft = state.getTimeLeft();
      if (myPerformer.isSilentLoginAllowed() && (timeLeft > 0) && (ourTimeToRelogin > timeLeft)) {
        LOG.debug("doing preventing relogin");
        return myPerformer.loginWithStoredPassword();
      }
    }
    return state;
  }

  private LoginState checkLoggedOrSilent() {
    LOG.debug("try checkLoggedOrSilent");
    final LoginState state = checkState();
    if (state.isSuccess()) {
      LOG.debug("login state success (checkLoggedOrSilent)");
      return state;
    }
    LOG.debug("login state not logged");
    if ((! state.isSuccess()) && (state.getError() != null)) {
      LOG.debug("error not null -> must be connection problem");
      return state;
    }
    if (myPerformer.isSilentLoginAllowed()) {
      LOG.debug("silent login allowed, logging");
      return myPerformer.loginWithStoredPassword();
    }
    return state;
  }

  private LoginState silentReconnect(final String error) {
    LOG.debug("try silent reconnect");
    if (myPerformer.isSilentLoginAllowed()) {
      LOG.debug("silent reconnect allowed, logging..");
      return myPerformer.loginWithStoredPassword();
    }
    return new LoginState(false, -1, error);
  }

  private LoginState ensureImpl(boolean ignoreDelays) {
    try {
      if (myRecentTime == -1) {
        LOG.debug("init state");
        return checkLoggedOrSilent();
      }
      final long time = System.currentTimeMillis() - myRecentTime;
      LOG.debug("ensure, recent time: " + myRecentTime + ", time: " + time + ", ignoreDelays: " + ignoreDelays);
      final boolean inBlindInterval = (!ignoreDelays) && (time < ourSuccessBlindInterval);
      if (mySuccess) {
        LOG.debug("currently success");
        if (inBlindInterval) {
          LOG.debug("success blind interval");
          return new LoginState(true, -1, null);
        }
        return checkLoggedOrSilent();
      }

      final LoginState currentState = checkState();
      if (currentState.isSuccess()) {
        LOG.debug("turned out state is success");
        return new LoginState(true, -1, null);
      }
      if (! myConnectionProblem) {
        LOG.debug("currently credentials problem");
        if ((! ignoreDelays) && (! myPerformer.isCredentialsChanged()) && (time < ourCredentialsBlindInterval)) {
          LOG.debug("credentials hasn't changed");
          return new LoginState(false, -1, null);
        }
        return silentReconnect(null);
      }
      LOG.debug("currently connection problem");
      if (time < ourNetworkBlindInterval) {
        LOG.debug("connection blind interval");
        return new LoginState(false, -1, myErrorMessage);
      }
      return silentReconnect(myErrorMessage);
    }
    catch (Throwable e) {
      LOG.info(e);
      throw new RuntimeException(e);
    }
  }

  private LoginState executeUnderLock(final Getter<LoginState> getter) {
    boolean triggerChangesUpdate;
    final LoginState result;
    synchronized (myLock) {
      boolean logged = mySuccess;
      result = getter.get();
      registerResult(result);
      triggerChangesUpdate = (! logged) && mySuccess;
    }
    if (triggerChangesUpdate) {
      LOG.debug("reconnected");
      myLoginStateListener.reconnected();
    }
    return result;
  }

  public LoginState ensure(final boolean ignoreDelays) {
    return executeUnderLock(new Getter<LoginState>() {
      public LoginState get() {
        return ensureImpl(ignoreDelays);
      }
    });
  }

  public void failed(final boolean connectionProblem, final String errorMessage) {
    synchronized (myLock) {
      myConnectionProblem = connectionProblem;
      myErrorMessage = errorMessage;
      mySuccess = false;
      fillTime();
      logSuccessOrFailure(false);
    }
  }

  private void logSuccessOrFailure(final boolean success) {
    LOG.debug("Reported: " + (success ? "logged" : "not logged") + ", time: " + myRecentTime);
  }

  public long recommendedLazyInterval() {
    return ourLazyInterval;
  }

  public static long defaultLazyInterval() {
    return ourLazyInterval;
  }
}
