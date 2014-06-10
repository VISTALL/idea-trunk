package com.intellij.seam.fileEditor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;

public class PageflowDesignerFileEditorProvider extends PerspectiveFileEditorProvider {

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

    return psiFile instanceof XmlFile &&  PageflowDomModelManager.getInstance(project).isPageflow((XmlFile)psiFile);
  }

  @NotNull
  public PerspectiveFileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new PageflowDesignerFileEditor(project, file);
  }

  public double getWeight() {
    return 0;
  }
}
