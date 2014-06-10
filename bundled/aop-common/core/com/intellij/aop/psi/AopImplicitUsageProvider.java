/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.aop.jam.AopConstants;

/**
 * @author peter
 */
public class AopImplicitUsageProvider implements ImplicitUsageProvider {
  public boolean isImplicitUsage(final PsiElement element) {
    if (element instanceof PsiParameter) {
      final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      if (method != null && method.getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) != null) {
        return true;
      }
    }
    return false;
  }

  public boolean isImplicitRead(final PsiElement element) {
    return false;
  }

  public boolean isImplicitWrite(final PsiElement element) {
    return false;
  }
}
