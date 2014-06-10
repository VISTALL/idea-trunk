/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.GenericValue;

/**
 * @author peter
 */
public interface AopPointcut {
  GenericValue<PsiPointcutExpression> getExpression();

  GenericValue<String> getQualifiedName();

  PsiElement getIdentifyingPsiElement();

  int getParameterCount();

}
