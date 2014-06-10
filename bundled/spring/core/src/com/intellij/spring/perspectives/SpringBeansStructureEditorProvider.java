/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.perspectives;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;

public class SpringBeansStructureEditorProvider extends PerspectiveFileEditorProvider {

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

    return psiFile instanceof XmlFile &&  SpringManager.getInstance(project).getSpringModelByFile((XmlFile)psiFile) != null;
  }

  @NotNull
  public PerspectiveFileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new SpringBeansStructureEditor(project, file);
  }

  public double getWeight() {
    return 0;
  }
}