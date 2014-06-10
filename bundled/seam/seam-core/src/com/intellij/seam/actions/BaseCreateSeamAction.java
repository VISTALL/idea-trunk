package com.intellij.seam.actions;

import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.seam.facet.SeamFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseCreateSeamAction extends AnAction {
  public static final Logger LOG = Logger.getInstance(CreateSeamComponentsAction.class.getName());

  public BaseCreateSeamAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  public final void actionPerformed(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (view == null) {
      return;
    }

    final PsiDirectory dir = view.getOrChooseDirectory();
    if (dir == null) return;

    final PsiElement createdElement = create(dir);
    if (createdElement != null) {
      view.selectElement(createdElement);
    }
  }

  public void update(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();

    final boolean enabled = isAvailable(dataContext);

    presentation.setVisible(enabled);
    presentation.setEnabled(enabled);
  }

  protected boolean isAvailable(final DataContext dataContext) {
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return false;
    }

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (view == null || view.getDirectories().length == 0) {
      return false;
    }

    final Module module = LangDataKeys.MODULE.getData(dataContext);

    return module != null && SeamFacet.getInstance(module) != null && isDirectoryAccepted(dataContext);
  }

  private boolean isDirectoryAccepted(final DataContext dataContext) {
    Module module = LangDataKeys.MODULE.getData(dataContext);
    if (module == null) {
      return false;
    }

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (view != null && project != null) {
      ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      PsiDirectory[] dirs = view.getDirectories();
      for (PsiDirectory dir : dirs) {
        if (dir.findFile(getFileName()) != null) continue;

        if ((isAllowedInSourceDir() && projectFileIndex.isInSourceContent(dir.getVirtualFile()) && JavaDirectoryService.getInstance().getPackage(dir) != null) ||
            (isAllowedInWebInf() && "WEB-INF".equals(dir.getName()))) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isAllowedInWebInf() {
    return true;
  }

  protected boolean isAllowedInSourceDir() {
    return true;
  }

  @Nullable
  private PsiElement create(final PsiDirectory directory) {
    final Module module = ModuleUtil.findModuleForPsiElement(directory);

    PsiElement psiElement = null;
    try {
      psiElement = FileTemplateUtil.createFromTemplate(getTemplate(module), getFileName(), null, directory);
    } catch (Exception e) {
      LOG.error(e);
    }

    return psiElement;
  }


  @NotNull
  protected abstract FileTemplate getTemplate(final Module module);

  @NotNull
  protected abstract String getFileName();
}

