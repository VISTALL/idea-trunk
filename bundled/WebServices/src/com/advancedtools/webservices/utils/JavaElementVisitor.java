package com.advancedtools.webservices.utils;

import com.intellij.psi.*;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;

/**
 * @author Maxim
 */
public class JavaElementVisitor extends PsiElementVisitor {
  public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
  }

  public void visitClass(PsiClass psiClass) {}

  public void visitField(PsiField psiField) {}

  public void visitMethod(PsiMethod psiMethod) {
  }

  public void visitElement(final PsiElement element) {
    if (EnvironmentFacade.isDianaOrBetter()) {
      if (element instanceof PsiClass) visitClass((PsiClass)element);
      else if (element instanceof PsiMethod) visitMethod((PsiMethod)element);
      else if (element instanceof PsiField) visitField((PsiField)element);
    }
  }
}