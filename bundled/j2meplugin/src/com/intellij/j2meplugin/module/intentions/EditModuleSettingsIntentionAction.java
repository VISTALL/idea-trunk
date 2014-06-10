package com.intellij.j2meplugin.module.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.MobileModuleUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditModuleSettingsIntentionAction extends PsiElementBaseIntentionAction {
  private static final Logger LOG = Logger.getInstance("#" + EditModuleSettingsIntentionAction.class.getName());

  @NotNull
  public String getText() {
    return J2MEBundle.message("edit.mobile.module.settings.intention.title");
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
    if (element == null) return false;
    final PsiFile psiFile = element.getContainingFile();
    if (psiFile == null) return false;
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) return false;
    final Module module = ModuleUtil.findModuleForFile(virtualFile, project);
    if (module == null) return false;
    return MobileModuleUtil.isExecutable(PsiTreeUtil.getParentOfType(element, PsiClass.class), module);
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final VirtualFile virtualFile = file.getVirtualFile();
    LOG.assertTrue(virtualFile != null);
    final Module module = ModuleUtil.findModuleForFile(virtualFile, project);
    LOG.assertTrue(module != null);
    ProjectSettingsService.getInstance(project).showModuleConfigurationDialog(module.getName(), J2MEBundle.message("mobile.module.settings.title"), false);
  }

  public boolean startInWriteAction() {
    return false;
  }
}