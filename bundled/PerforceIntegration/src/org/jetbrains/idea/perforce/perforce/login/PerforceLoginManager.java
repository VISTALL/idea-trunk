package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public interface PerforceLoginManager {
  long ensure(final P4Connection connection);
  boolean check(final P4Connection connection1) throws VcsException;

  boolean silentLogin(final P4Connection connection) throws VcsException;

  void notLogged(final P4Connection connection);
}
