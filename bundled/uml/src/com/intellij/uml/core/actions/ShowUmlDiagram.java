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

package com.intellij.uml.core.actions;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.uml.UmlProvider;
import com.intellij.uml.UmlVirtualFileSystem;

/**
 * @author Konstantin Bulenkov
 */
public class ShowUmlDiagram extends ShowUmlBase {
  @Override
  protected Runnable show(Object element, UmlProvider provider, final Project project, RelativePoint point) {
    final String url = UmlVirtualFileSystem.PROTOCOL_PREFIX + provider.getID() + "/" + provider.getVfsResolver().getQualifiedName(element);
    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(url);

    if (virtualFile instanceof UmlVirtualFileSystem.UmlVirtualFile) {
      final UmlVirtualFileSystem.UmlVirtualFile file = (UmlVirtualFileSystem.UmlVirtualFile)virtualFile;
      final String text = provider.getElementManager().getElementTitle(element);
      if (text != null) {
        file.setPresentableName(text);
      }
      UmlVirtualFileSystem.setInitialized(file);
      return new Runnable() {
        public void run() {
          FileEditorManager.getInstance(project).openFile(file, true);
        }
      };
    }
    return null;
  }
}
