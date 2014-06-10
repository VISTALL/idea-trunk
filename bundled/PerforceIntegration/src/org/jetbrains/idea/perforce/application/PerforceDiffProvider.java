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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

public class PerforceDiffProvider implements DiffProvider {
  private final Project myProject;
  @NonNls private static final String REVISION_NONE = "none";

  public PerforceDiffProvider(final Project project) {
    myProject = project;
  }

  public VcsRevisionNumber getCurrentRevision(VirtualFile file) {
    try {
      long revNumber = PerforceRunner.getInstance(myProject).haveRevision(P4File.create(file));
      if (revNumber < 0) {
        return null;
      }
      return new VcsRevisionNumber.Long(revNumber);
    }
    catch (VcsException e) {
      return null;
    }
  }

  private static boolean isInvalidRevision(final String revision) {
    return revision == null || revision.length() == 0 || revision.equals(REVISION_NONE);
  }

  public ItemLatestState getLastRevision(VirtualFile file) {
    try {
      final FStat fstat = P4File.create(file).getFstat(myProject, false);
      final String headRev = fstat.headRev;
      if (isInvalidRevision(headRev)) {
        return null;
      }
      return new ItemLatestState(new VcsRevisionNumber.Long(Long.parseLong(headRev)), true);
    }
    catch (VcsException e) {
      return null;
    }
  }

  public ContentRevision createFileContent(final VcsRevisionNumber revisionNumber, final VirtualFile selectedFile) {
    final long revNumber;
    if (revisionNumber instanceof PerforceVcsRevisionNumber) {
      revNumber = ((PerforceVcsRevisionNumber)revisionNumber).getRevisionNumber();
    }
    else {
      revNumber = ((VcsRevisionNumber.Long) revisionNumber).getLongValue();
    }
    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(selectedFile);
    if (selectedFile.getFileType().isBinary()) {
      return new PerforceBinaryContentRevision(myProject, filePath, revNumber);
    }
    return new PerforceContentRevision(myProject, filePath, revNumber);
  }

  public ItemLatestState getLastRevision(FilePath filePath) {
    try {
      final FStat fstat = P4File.create(filePath).getFstat(myProject, false);
      final String headRev = fstat.headRev;
      if (isInvalidRevision(headRev)) {
        return null;
      }
      return new ItemLatestState(new VcsRevisionNumber.Long(Long.parseLong(headRev)), true);
    }
    catch (VcsException e) {
      return null;
    }
  }

  public VcsRevisionNumber getLatestCommittedRevision(VirtualFile vcsRoot) {
    // todo
    return null;
  }
}
