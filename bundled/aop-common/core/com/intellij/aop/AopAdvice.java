/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public interface AopAdvice extends CommonModelElement {

  @Nullable
  PsiPointcutExpression getPointcutExpression();

  @NotNull
  AopAdviceType getAdviceType();

  /**
   * use com.intellij.aop.AopAdviceUtil#accepts(AopAdvice, com.intellij.psi.PsiMethod) 
   */
  PointcutMatchDegree accepts(PsiMethod method);

  @Nullable
  AopAdvisedElementsSearcher getSearcher();
}
