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

package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Refreshes VFS; refreshes VCS statuses as needed
 */
public class RefreshForVcs {
  private final Collection<VirtualFile> myFiles;
  // recursively for simplicity
  private final Collection<VirtualFile> myDirs;
  private final LocalFileSystem myLfs;

  public RefreshForVcs() {
    myFiles = new LinkedList<VirtualFile>();
    myDirs = new LinkedList<VirtualFile>();
    myLfs = LocalFileSystem.getInstance();
  }

  public void refreshFile(final File file) {
    refresh(file);
  }

  public void addDeletedFile(final File file) {
    refreshDeletedOrReplaced(file);
  }

  public void addDir(final File file) {
    refreshDir(file);
  }

  public void run(final Project project) {
    VcsDirtyScopeManager.getInstance(project).filesDirty(myFiles, myDirs);
  }

  // for created/existing
  private void refresh(final File root) {
    VirtualFile vFile = myLfs.refreshAndFindFileByIoFile(root);
    if (vFile != null) {
      vFile.refresh(false, false);
      myFiles.add(vFile);
    }
  }

  private void refreshDeletedOrReplaced(final File root) {
    refreshDir(root.getParentFile());
  }

  private void refreshDir(final File dir) {
    if (dir == null) return;
    final VirtualFile vf = myLfs.refreshAndFindFileByIoFile(dir);
    if (vf == null) {
      refreshDir(dir.getParentFile());
      return;
    }
    myDirs.add(vf);
    ((NewVirtualFile) vf).markDirtyRecursively();
    vf.refresh(false, true);
  }
}
