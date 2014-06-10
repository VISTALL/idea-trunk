package org.jetbrains.idea.perforce.perforce.connections;

import org.jetbrains.idea.perforce.StreamGobbler;
import com.intellij.openapi.vcs.impl.ProcessWaiter;

import java.io.InputStream;

public class PerforceProcessWaiter extends ProcessWaiter<StreamGobbler> {
  @Override
  protected boolean tryReadStreams(int rc) {
    return rc != P4CommandLineConnection.TIMEOUT_EXIT_CODE;
  }

  protected StreamGobbler createStreamListener(InputStream stream) {
    return new StreamGobbler(stream);
  }

  public void clearGobblers() {
    if (myInStreamListener != null) {
      myInStreamListener.deleteTempFile();
    }
    if (myErrStreamListener != null) {
      myErrStreamListener.deleteTempFile();
    }
  }
}
