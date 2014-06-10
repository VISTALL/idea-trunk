/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.application;

import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.BinaryContentRevision;

/**
 * @author yole
 */
public class PerforceBinaryContentRevision extends PerforceContentRevision implements BinaryContentRevision {
  private byte[] myContent = null;

  public PerforceBinaryContentRevision(final Project project, final FilePath path, final long revision) {
    super(project, path, revision);
  }

  public PerforceBinaryContentRevision(final Project project, final P4Connection connection, final String depotPath, final long revision) {
    super(project, connection, depotPath, revision);
  }

  @Nullable
  public byte[] getBinaryContent() throws VcsException {
    if (myContent != null) return myContent;

    final P4File p4File = P4File.create(myFilePath);
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (!settings.ENABLED) return null;
    final FStat fstat = p4File.getFstat(myProject, false);
    final byte[] bytes = PerforceRunner.getInstance(myProject).getByteContent(p4File, fstat.haveRev);
    if (bytes != null) {
      myContent = bytes;
    }

    return myContent;
  }
}