/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.process.InterruptibleProcess;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.CancelActionException;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class P4CommandLineConnection {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.connections.P4CommandLineConnection");

  public static final int TIMEOUT_EXIT_CODE = -2;

  private P4CommandLineConnection() {
  }

  public static void runP4Command(PerforceSettings settings,
                                  String[] p4args,
                                  ExecResult retVal,
                                  final StringBuffer inputStream,
                                  final File cwd)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException {
    executeP4CommandLine(settings, p4args, retVal, inputStream, cwd);
  }

  static private ExecResult executeP4CommandLine(final PerforceSettings settings,
                                                              @NonNls final String[] p4args,
                                                              ExecResult retVal,
                                                              StringBuffer inputData, final File cwd) {
    try {
      runCmdLine(settings, p4args, retVal, inputData, cwd);
    }
    catch (CancelActionException e) {
      throw e;
    }
    catch (Exception e) {
      retVal.setException(e);
    }

    return retVal;
  }

  private static void runCmdLine(final PerforceSettings settings,
                                 @NonNls final String[] p4args,
                                 final ExecResult retVal,
                                 StringBuffer inputData,
                                 File cwd)
    throws IOException,
           InterruptedException, PerforceTimeoutException {
    final String[] connArgs = settings.getConnectArgs();
    final String[] cmd = new String[1 + connArgs.length + p4args.length];
    int c = 0;
    cmd[c++] = settings.getPathToExec();
    for (String connArg : connArgs) {
      cmd[c++] = connArg;
    }
    for (String p4arg : p4args) {
      cmd[c++] = p4arg;
    }

    debugCmd(cwd, cmd);

    final Runtime rt = Runtime.getRuntime();
    String[] env = EnvironmentUtil.getEnvironment();

    // On Unix, Perforce relies on the "PWD" variable to determine its current working directory
    // for finding .p4config.  We need to make sure it matches the directory we want to use.
    // (JetBrains bugs: IDEADEV-7445, etc.)
    setEnvironmentVariable(env, "PWD", cwd.getAbsolutePath());

    int rc;
    Process proc = null;
    MyInterruptibleProcess worker = null;
    PerforceProcessWaiter processWaiter = null;
    try {
      proc = rt.exec(cmd, env, cwd);
      if (inputData != null) {
        passInputToProcess(inputData, proc);
      }

      worker = new MyInterruptibleProcess(settings.getProject(), proc, settings.SERVER_TIMEOUT);

      processWaiter = new PerforceProcessWaiter();
      final PerforceProcessWaiter processWaiterCopy = processWaiter;
      worker.setOnBeforeInterrupt(new Runnable() {
        public void run() {
          processWaiterCopy.cancelListeners();
        }
      });
      rc = processWaiter.execute(worker, settings.SERVER_TIMEOUT);
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e.getCause());
    }
    catch (TimeoutException e) {
      rc = -2;
    } finally {
      if (worker != null) {
        worker.closeProcess();
      } else if ((worker == null) && (proc != null)) {
        InterruptibleProcess.close(proc);
      }
    }

    if (rc == 0) {
      retVal.setExitCode(worker.getExitCode());
      retVal.setOutputGobbler(processWaiter.getInStreamListener());
      retVal.setErrorGobbler(processWaiter.getErrStreamListener());
    }
    else if (rc == TIMEOUT_EXIT_CODE) {
      processWaiter.clearGobblers();
      throw new PerforceTimeoutException();
    }
    else {
      if (settings.canBeChanged()) {
        settings.disable();
      }
      processWaiter.clearGobblers();
      throw new PerforceTimeoutException();
    }
  }

  private static void passInputToProcess(StringBuffer inputData, Process proc) throws IOException {
    final OutputStream outputStream = proc.getOutputStream();

    byte[] bytes = inputData.toString().getBytes();
    outputStream.write(bytes);
    // must close or p4 won't read input
    outputStream.close();
  }

  private static void debugCmd(File cwd, String[] cmd) {
    if (LOG.isDebugEnabled()) {
      final StringBuffer cmdLabel = new StringBuffer();
      for (String aCmd : cmd) {
        cmdLabel.append(" ");
        cmdLabel.append(aCmd);
      }
      LOG.debug("[Perf Execute:] " + cmdLabel.toString() + "[cwd] " + cwd);
    }
  }

  private static class MyInterruptibleProcess extends InterruptibleProcess {
    private boolean myNeedStallDialog;
    private final Project myProject;
    // todo think & refactor
    // to be able to notify gobblers that streams are closed by parent
    private Runnable myOnBeforeInterrupt;

    private MyInterruptibleProcess(final Project project, final Process process, final long timeout) {
      super(process, timeout, TimeUnit.MILLISECONDS);
      myProject = project;
      myNeedStallDialog = StallConnectionUtil.needDialog();
    }

    @Override
    public void closeProcess() {
      if (myOnBeforeInterrupt != null) {
        myOnBeforeInterrupt.run();
      }
      super.closeProcess();
    }

    public void setOnBeforeInterrupt(Runnable onBeforeInterrupt) {
      myOnBeforeInterrupt = onBeforeInterrupt;
    }

    protected int processTimeout() {
      return StallConnectionUtil.requestUser();
    }

    @Override
    protected int processTimeoutInEDT() {
      if (! myNeedStallDialog || myProject.isDisposed()) {
        return TIMEOUT_EXIT_CODE;
      }
      return super.processTimeoutInEDT();
    }
  }

  private static void setEnvironmentVariable(String[] env, @NonNls String name, String newValue) {
    for (int i = 0; i < env.length; i++) {
      String var = env[i];
      if (StringUtil.startsWithConcatenationOf(var, name, "=")) {
        env[i] = name + "=" + newValue;
      }
    }
  }
}
