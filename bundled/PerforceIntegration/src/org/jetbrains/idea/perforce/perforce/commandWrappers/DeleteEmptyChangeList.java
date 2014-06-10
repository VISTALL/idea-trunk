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
import org.jetbrains.idea.perforce.perforce.CommandArguments;
import org.jetbrains.idea.perforce.perforce.P4Command;
import org.jetbrains.idea.perforce.perforce.RunnerForCommands;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public class DeleteEmptyChangeList extends PerforcelyRunnable<DeleteEmptyChangeList> {
  public static final SharedErrorsEnum<DeleteEmptyChangeList> NOT_EMPTY =
    new SharedErrorsEnum.Contained<DeleteEmptyChangeList>("open file(s) associated with it and can't be deleted");
  public static final SharedErrorsEnum<DeleteEmptyChangeList> NOT_FOUND =
    new SharedErrorsEnum.Patterned<DeleteEmptyChangeList>("Change [0-9]+ unknown.");

  private long myNumber;

  public DeleteEmptyChangeList(final RunnerForCommands runner, final P4Connection connection, final long number) {
    super(P4Command.change, runner, connection);
    myNumber = number;
  }

  @Override
  protected void checkArguments() throws VcsException {
  }

  @Override
  protected void addArguments(final CommandArguments arguments) {
    arguments.append("-d");
    arguments.append(String.valueOf(myNumber));
  }
}
