package com.intellij.spring.model.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spring.model.actions.generate.GenerateSpringBeanDependenciesUtil;
import com.intellij.spring.model.actions.generate.SpringGenerateTemplatesHolder;
import com.intellij.spring.model.xml.beans.SpringInjection;

import java.util.List;

public class GenerateSpringBeanDependenciesActionHandler implements CodeInsightActionHandler {
  private final boolean mySetterDependency;

  public GenerateSpringBeanDependenciesActionHandler(final boolean setterDependency) {
    mySetterDependency = setterDependency;
  }

  public boolean isSetterDependency() {
    return mySetterDependency;
  }

  public void invoke(Project project, Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element != null) {
      final PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);

      final List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = new WriteCommandAction<List<Pair<SpringInjection,SpringGenerateTemplatesHolder>>>(psiClass.getProject()) {
        protected void run(Result<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>> result) throws Throwable {
          final List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = GenerateSpringBeanDependenciesUtil
                  .generateDependenciesFor(GenerateSpringBeanDependenciesUtil.getSpringModel(psiClass), psiClass, mySetterDependency);

          result.setResult(list);
        }
      }.execute().getResultObject();

      for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
         pair.getSecond().runTemplates();
      }
    }
  }

  public boolean startInWriteAction() {
    return false;
  }
}
