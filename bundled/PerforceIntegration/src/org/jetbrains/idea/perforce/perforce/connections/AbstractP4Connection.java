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

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;

import java.io.File;
import java.io.IOException;

public abstract class AbstractP4Connection implements P4Connection {
  public void runP4Command(PerforceSettings settings, String[] p4args, ExecResult retVal, @Nullable final StringBuffer inputStream)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException {

    P4CommandLineConnection.runP4Command(settings, p4args, retVal, inputStream, getCwd());
  }

  protected abstract File getCwd();

  public ExecResult runP4CommandLine(final PerforceSettings settings,
                                                  final String[] strings,
                                                  final StringBuffer stringBuffer) throws VcsException {
    final ExecResult result = new ExecResult();
    try {
      P4CommandLineConnection.runP4Command(settings, strings, result, stringBuffer, getCwd());
    }
    catch (PerforceTimeoutException e) {
      throw new VcsException(e);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
    catch (InterruptedException e) {
      throw new VcsException(e);
    }
    return result;
  }
}
