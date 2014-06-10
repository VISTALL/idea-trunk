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

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.idea.perforce.perforce.ConnectionId;

import java.io.File;
import java.io.IOException;

public class P4ConfigFileBasedConnection extends AbstractP4Connection{

  private final File myWorkingDir;
  private final String myP4configFileName;

  public P4ConfigFileBasedConnection(final File workingDir, final String p4configFileName) {
    myWorkingDir = workingDir;
    myP4configFileName = p4configFileName;
  }

  public ConnectionId getId() {
    return new ConnectionId(myP4configFileName, myWorkingDir.getPath());
  }

  public boolean handlesFile(File file) {
    try {
      return FileUtil.isAncestor(myWorkingDir, file, false);
    }
    catch (IOException e) {
      return false;
    }
  }

  public boolean isValid() {
    return new File(myWorkingDir, myP4configFileName).isFile();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final P4ConfigFileBasedConnection that = (P4ConfigFileBasedConnection)o;

    if (!myP4configFileName.equals(that.myP4configFileName)) return false;
    return myWorkingDir.equals(that.myWorkingDir);

  }

  public int hashCode() {
    int result;
    result = myWorkingDir.hashCode();
    result = 29 * result + myP4configFileName.hashCode();
    return result;
  }

  protected File getCwd() {
    return myWorkingDir;
  }
}
