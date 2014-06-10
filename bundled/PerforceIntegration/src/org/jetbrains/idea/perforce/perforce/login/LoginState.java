package org.jetbrains.idea.perforce.perforce.login;

public class LoginState {
  private final boolean mySuccess;
  private final long myTimeLeft;
  private final String myError;

  public LoginState(boolean success, long timeLeft, String error) {
    mySuccess = success;
    myTimeLeft = timeLeft;
    myError = error;
  }

  public boolean isSuccess() {
    return mySuccess;
  }

  public long getTimeLeft() {
    return myTimeLeft;
  }

  public String getError() {
    return myError;
  }
}
