/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.spring.model.xml.aop.AfterReturningAdvice;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;

/**
 * @author peter
 */
public abstract class AfterReturningAdviceImpl extends BasicAdviceImpl implements AfterReturningAdvice {
  public PointcutMatchDegree accepts(final PsiMethod method) {
    final PsiParameter parameter = getReturning().getValue();
    if (parameter != null) {
      final PsiType returnType = method.getReturnType();
      if (returnType != null && !parameter.getType().isAssignableFrom(returnType)) return PointcutMatchDegree.FALSE;
    }
    return super.accepts(method);
  }
}
