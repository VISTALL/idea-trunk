/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop;

import com.intellij.psi.PsiMethod;
import com.intellij.aop.psi.PointcutMatchDegree;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class AopAdviceUtil {

  public static PointcutMatchDegree accepts(AopAdvice advice, PsiMethod method) {
    return accepts(advice, method, advice.getSearcher());
  }

  public static PointcutMatchDegree accepts(AopAdvice advice, PsiMethod method, @Nullable AopAdvisedElementsSearcher searcher) {
    if (searcher != null && searcher.acceptsBoundMethod(method) &&
        advice.accepts(method) == PointcutMatchDegree.TRUE &&
        searcher.acceptsBoundMethodHeavy(method)) {
      return PointcutMatchDegree.TRUE;
    }
    return PointcutMatchDegree.FALSE;
  }

}
