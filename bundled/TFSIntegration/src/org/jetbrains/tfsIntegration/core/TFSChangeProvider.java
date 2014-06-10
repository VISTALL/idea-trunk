/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.tfsIntegration.core;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.tfsIntegration.core.tfs.*;
import org.jetbrains.tfsIntegration.exceptions.TfsException;

import java.text.MessageFormat;
import java.util.List;

/**
 * TODO important cases
 * 1. when folder1 is unversioned and folder1/file1 is scheduled for addition, team explorer effectively shows folder1 as scheduled for addition
 */

public class TFSChangeProvider implements ChangeProvider {

  private final Project myProject;

  public TFSChangeProvider(final Project project) {
    myProject = project;
  }

  public boolean isModifiedDocumentTrackingRequired() {
    return true;
  }

  public void doCleanup(final List<VirtualFile> files) {
  }

  public void getChanges(final VcsDirtyScope dirtyScope,
                         final ChangelistBuilder builder,
                         final ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    if (myProject.isDisposed()) {
      return;
    }
    if (builder == null) {
      return;
    }

    progress.setText("Processing changes");

    // process only roots, filter out child items since requests are recursive anyway
    RootsCollection.FilePathRootsCollection roots = new RootsCollection.FilePathRootsCollection();
    roots.addAll(dirtyScope.getRecursivelyDirtyDirectories());

    final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
    for (FilePath dirtyFile : dirtyScope.getDirtyFiles()) {
      // workaround for IDEADEV-31511 and IDEADEV-31721
      if (dirtyFile.getVirtualFile() == null || !changeListManager.isIgnoredFile(dirtyFile.getVirtualFile())) {
        roots.add(dirtyFile);
      }
    }

    if (roots.isEmpty()) {
      return;
    }

    try {
      final Ref<Boolean> mappingFound = Ref.create(false);
      // ingore orphan roots here
      WorkstationHelper.processByWorkspaces(roots, true, new WorkstationHelper.VoidProcessDelegate() {
        public void executeRequest(final WorkspaceInfo workspace, final List<ItemPath> paths) throws TfsException {
          StatusProvider.visitByStatus(workspace, paths, true, progress, new ChangelistBuilderStatusVisitor(myProject, builder, workspace));
          mappingFound.set(true);
        }
      });
      if (!mappingFound.get()) {
        final String message;
        if (roots.size() > 1) {
          message = "Team Foundation Server mappings not found";
        }
        else {
          FilePath orphan = roots.iterator().next();
          message = MessageFormat.format("Team Foundation Server mappings not found for ''{0}''", orphan.getPresentableUrl());
        }
        throw new VcsException(message);
      }
    }
    catch (TfsException e) {
      throw new VcsException(e.getMessage(), e);
    }
  }

}
