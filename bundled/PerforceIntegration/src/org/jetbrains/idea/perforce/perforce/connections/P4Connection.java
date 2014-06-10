package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;

import java.io.IOException;
import java.io.File;

public interface P4Connection {
  ConnectionId INVALID_CONNECTION_ID = new ConnectionId() {
    @Override
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  };
  @NonNls String CANNOT_EXECUTE = "Cannot execute: invalid connection";
  P4Connection INVALID = new P4Connection() {
    public void runP4Command(PerforceSettings settings, String[] p4args, ExecResult retVal, final StringBuffer inputStream)
      throws VcsException, PerforceTimeoutException, IOException, InterruptedException {
      retVal.setException(new RuntimeException(CANNOT_EXECUTE));
    }

    public ExecResult runP4CommandLine(final PerforceSettings settings,
                                                    final String[] strings,
                                                    final StringBuffer stringBuffer) throws VcsException {
      final ExecResult execResult = new ExecResult();
      execResult.setException(new RuntimeException(CANNOT_EXECUTE));
      return execResult;
    }

    public ConnectionId getId() {
      return INVALID_CONNECTION_ID;
    }

    public boolean handlesFile(File file) {
      return false;
    }
  };

  void runP4Command(PerforceSettings settings, String[] p4args, ExecResult retVal, @Nullable final StringBuffer inputStream)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException;

  ExecResult runP4CommandLine(final PerforceSettings settings, @NonNls final String[] strings, final StringBuffer stringBuffer)
    throws VcsException;

  ConnectionId getId();

  boolean handlesFile(File file);
}
