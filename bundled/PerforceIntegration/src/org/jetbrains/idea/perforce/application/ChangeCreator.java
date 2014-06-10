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

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.util.Consumer;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;
import org.jetbrains.idea.perforce.perforce.ResolvedFile;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;

public class ChangeCreator {
  private final Project myProject;
  private Consumer<Change> myCreationListener;

  public ChangeCreator(final Project project) {
    myProject = project;
  }

  public void setCreationListener(Consumer<Change> creationListener) {
    myCreationListener = creationListener;
  }

  /*
  @Nullable
  public Change createChange(final FilePath path, PerforceAbstractChange perforceChange,
                              final Collection<VirtualFile> resolvedWithConflicts, long haveRevision) {
    VirtualFile file = path.getVirtualFile();
    boolean isResolvedWithConflict = resolvedWithConflicts.contains(file);

    if (perforceChange.getType() == PerforceAbstractChange.ADD || perforceChange.getType() == PerforceAbstractChange.BRANCH) {
      return createAddedFileChange(path, isResolvedWithConflict);
    }
    else if (perforceChange.getType() == PerforceAbstractChange.DELETE) {
      return createDeletedFileChange(path, haveRevision, isResolvedWithConflict);
    }
    else if (perforceChange.getType() == PerforceAbstractChange.EDIT || perforceChange.getType() == PerforceAbstractChange.INTEGRATE) {
      return createEditedFileChange(path, haveRevision, isResolvedWithConflict);
    }

    //TODO: UNKNOWN
    return null;
  } */

  public Change createEditedFileChange(final FilePath path, final long haveRevision, boolean isResolvedWithConflict) {
    return createChange(PerforceCachingContentRevision.create(myProject, path, haveRevision),
                      CurrentContentRevision.create(path),
                      isResolvedWithConflict ? FileStatus.MERGE : FileStatus.MODIFIED);
  }

  public Change createAddedFileChange(final FilePath path, boolean isResolvedWithConflict) {
    return createChange(null, CurrentContentRevision.create(path),
                      isResolvedWithConflict ? FileStatus.MERGE : FileStatus.ADDED);
  }

  public Change createDeletedFileChange(final File file, final long haveRevision, boolean isResolvedWithConflict) {
    return createChange(PerforceCachingContentRevision.create(myProject, FilePathImpl.createForDeletedFile(file, false), haveRevision), null,
                      isResolvedWithConflict ? FileStatus.MERGE : FileStatus.DELETED);
  }

  public Change createDeletedFileChange(final FilePath path, final long haveRevision, boolean isResolvedWithConflict) {
    return createChange(PerforceCachingContentRevision.create(myProject, path, haveRevision), null,
                      isResolvedWithConflict ? FileStatus.MERGE : FileStatus.DELETED);
  }

  public Change createRenameChange(final P4Connection connection, final ResolvedFile resolvedFile, final FilePath afterPath) {
    long revision = resolvedFile.getRevision2();
    if (revision < 0) {
      revision = resolvedFile.getRevision1();
    }
    // TODO CACHE IT!!!!
    ContentRevision beforeRevision = PerforceContentRevision.create(myProject, connection, resolvedFile.getDepotPath(), revision);
    ContentRevision afterRevision = CurrentContentRevision.create(afterPath);
    return createChange(beforeRevision, afterRevision, FileStatus.MODIFIED);
  }

  private Change createChange(final ContentRevision before, final ContentRevision after, final FileStatus fileStatus) {
    final Change change = new Change(before, after, fileStatus);
    if (myCreationListener != null) {
      myCreationListener.consume(change);
    }
    return change;
  }
}
