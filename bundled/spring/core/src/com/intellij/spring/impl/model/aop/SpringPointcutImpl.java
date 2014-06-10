/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.psi.AopReferenceExpression;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.spring.model.xml.aop.SpringPointcut;
import gnu.trove.THashSet;

import java.util.Set;

/**
 * @author peter
 */
public abstract class SpringPointcutImpl implements SpringPointcut {
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  public int getParameterCount() {
    final PsiPointcutExpression expression = getExpression().getValue();
    if (expression == null) return -1;

    final Set<String> paramNames = new THashSet<String>();
    expression.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override public void visitElement(final PsiElement element) {
        if (element instanceof AopReferenceExpression) {
          final PsiElement psiElement = ((AopReferenceExpression)element).resolve();
          if (psiElement instanceof PsiParameter) {
            paramNames.add(((PsiParameter)psiElement).getName());
          }
        }
        super.visitElement(element);
      }
    });
    return paramNames.size();
  }

}
