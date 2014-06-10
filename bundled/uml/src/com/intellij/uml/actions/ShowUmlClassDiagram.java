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

package com.intellij.uml.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.uml.UmlVirtualFileSystem;
import com.intellij.uml.utils.UmlIcons;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class ShowUmlClassDiagram extends AnAction {
  public void update(final AnActionEvent e) {
    boolean enabled = getPsiClass(e) != null || getPsiPackage(e) != null;
    e.getPresentation().setVisible(enabled);
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setIcon(UmlIcons.UML_ICON);
  }

  @Nullable
  static PsiClass getPsiClass(final AnActionEvent e) {
    PsiElement element = DataKeys.PSI_ELEMENT.getData(e.getDataContext());
    PsiFile file = DataKeys.PSI_FILE.getData(e.getDataContext());
    if (element == null) {
      element = UmlUtils.getPsiClass(file);
    }

    if (element == null) return null;

    final String fqn = UmlUtils.getFQN(element);
    return ((element instanceof PsiClass) && fqn != null && fqn.length() > 0) 
           ? (PsiClass)element : null;
  }

  @Nullable
  private static PsiPackage getPsiPackage(final AnActionEvent e) {
    final PsiElement data = DataKeys.PSI_ELEMENT.getData(e.getDataContext());
    final PsiPackage psiPackage = UmlUtils.getPsiPackage(data);
    return psiPackage != null && psiPackage.getQualifiedName().length() > 0 ? psiPackage : null;
  }

  public void actionPerformed(AnActionEvent e) {
    PsiElement element = UmlUtils.getNotNull(getPsiClass(e), getPsiPackage(e));

    String fqn = UmlUtils.getFQN(element);
    Project project = DataKeys.PROJECT.getData(e.getDataContext());
    final String url = UmlVirtualFileSystem.PROTOCOL_PREFIX + fqn;
    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(url);

    if (virtualFile != null) {
      UmlVirtualFileSystem.setInitialized(virtualFile);
      FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }
  }
}
