package org.jetbrains.idea.perforce.perforce.login;

public interface AttemptsStateMachine {
  void successful();
  LoginState login(String password);
  LoginState ensure(boolean ignoreDelays);
  void failed(final boolean connectionProblem, final String errorMessage);
  long recommendedLazyInterval();
}
