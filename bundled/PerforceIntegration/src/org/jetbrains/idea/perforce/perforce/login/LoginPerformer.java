package org.jetbrains.idea.perforce.perforce.login;

public interface LoginPerformer {
  LoginState getLoginState();
  boolean isSilentLoginAllowed();
  boolean isCredentialsChanged();
  LoginState login(String password);
  LoginState loginWithStoredPassword();
}
