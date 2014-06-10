package com.intellij.spring.model.actions;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.actions.generate.GenerateSpringBeanDependenciesUtil;

public abstract class GenerateSpringBeanDependencyAction extends BaseGenerateAction {

  public GenerateSpringBeanDependencyAction(final GenerateSpringBeanDependenciesActionHandler handler, String text) {
    super(handler);
    getTemplatePresentation().setText(text);
    getTemplatePresentation().setIcon(SpringIcons.SPRING_ICON);
  }

  protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
    if (!super.isValidForFile(project, editor, file)) return false;
    final PsiClass psiClass = getTargetClass(editor, file);

    if (psiClass == null || psiClass.isInterface() || psiClass.isEnum()) return false;
    Module module = GenerateSpringBeanDependenciesUtil.getSpringModule(psiClass);

    return  module != null &&  GenerateSpringBeanDependenciesUtil.acceptPsiClass(psiClass, isSetterDependency());
  }

  private boolean isSetterDependency() {
    return ((GenerateSpringBeanDependenciesActionHandler)getHandler()).isSetterDependency();  
  }

  public void update(final AnActionEvent event) {
    super.update(event);
  }
}
