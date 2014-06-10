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

package org.jetbrains.android.facet;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import org.jetbrains.android.compiler.AndroidAptCompiler;
import org.jetbrains.android.compiler.AndroidCompileUtil;
import org.jetbrains.android.compiler.AndroidIdlCompiler;
import org.jetbrains.android.fileTypes.AndroidIdlFileType;
import static org.jetbrains.android.util.AndroidUtils.findSourceRoot;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 31, 2009
 * Time: 4:49:30 PM
 * To change this template use File | Settings | File Templates.
 */
class AndroidResourceFilesListener extends VirtualFileAdapter {
  private final AndroidFacet myFacet;

  public AndroidResourceFilesListener(AndroidFacet facet) {
    myFacet = facet;
  }

  @Override
  public void fileCreated(VirtualFileEvent event) {
    fileChanged(event);
  }

  @Override
  public void fileDeleted(VirtualFileEvent event) {
    fileChanged(event);
  }

  @Override
  public void fileMoved(VirtualFileMoveEvent event) {
    fileChanged(event);
  }

  @Override
  public void fileCopied(VirtualFileCopyEvent event) {
    fileChanged(event);
  }

  @Override
  public void contentsChanged(VirtualFileEvent event) {
    fileChanged(event);
  }

  private void fileChanged(@NotNull final VirtualFileEvent e) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        synchronized (myFacet.getLock()) {
          if (myFacet.isDisposed()) return;
          Module myModule = myFacet.getModule();
          Project project = myModule.getProject();
          if (project.isDisposed()) return;
          VirtualFile file = e.getFile();
          Module module = ModuleUtil.findModuleForFile(file, project);
          if (module == myModule) {
            VirtualFile parent = e.getParent();
            if (parent != null) {
              parent = parent.getParent();
              if (parent == AndroidRootUtil.getResourceDir(module) || AndroidRootUtil.getManifestFile(module) == file) {
                AndroidCompileUtil.generate(myModule, new AndroidAptCompiler());
                myFacet.getLocalResourceManager().invalidateAttributeDefinitions();
              }
              if (file.getFileType() == AndroidIdlFileType.ourFileType) {
                VirtualFile sourceRoot = findSourceRoot(myModule, file);
                if (sourceRoot != null && AndroidRootUtil.getGenDir(module) != sourceRoot) {
                  AndroidCompileUtil.generate(myModule, new AndroidIdlCompiler(project));
                }
              }
            }
          }
        }
      }
    });
  }
}
