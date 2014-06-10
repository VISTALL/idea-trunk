/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.idea.perforce.perforce.commandWrappers;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.CommandArguments;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.P4Command;
import org.jetbrains.idea.perforce.perforce.RunnerForCommands;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Collection;
import java.util.LinkedList;

abstract class PerforcelyRunnable<T extends PerforcelyRunnable> implements ThrowableRunnable<VcsException> {
  private final P4Command myCommand;
  private final RunnerForCommands myRunner;
  private final P4Connection myConnection;
  protected final Collection<SharedErrorsEnum<T>> myAcceptableErrors;

  protected PerforcelyRunnable(final P4Command command, final RunnerForCommands runner, final P4Connection connection) {
    myCommand = command;
    myRunner = runner;
    myConnection = connection;
    myAcceptableErrors = new LinkedList<SharedErrorsEnum<T>>();
  }

  protected abstract void addArguments(final CommandArguments arguments);
  protected abstract void checkArguments() throws VcsException;

  public void allowError(final SharedErrorsEnum<T> error) {
    myAcceptableErrors.add(error);
  }

  @Nullable
  protected StringBuffer getInputStream() {
    return null;
  }
  protected boolean justLog() {
    return false;
  }

  public void run() throws VcsException {
    checkArguments();
    
    final CommandArguments arguments = CommandArguments.createOn(myCommand);
    addArguments(arguments);
    final ExecResult execResult = myRunner.executeP4Command(arguments.getArguments(), myConnection, getInputStream(), justLog());
    if (execResult.getExitCode() != 0) {
      final String err = execResult.getStderr();
      for (SharedErrorsEnum<T> error : myAcceptableErrors) {
        if (error.matches(err)) return;
      }
      myRunner.checkError(execResult);
    }
  }
}
