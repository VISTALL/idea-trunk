/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiType;
import com.intellij.util.PairFunction;

/**
 * @author peter
 */
public class TypeArgumentMatcher implements PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> {
  public static final TypeArgumentMatcher NO_AUTOBOXING = new TypeArgumentMatcher();

  protected TypeArgumentMatcher() {
  }

  public PointcutMatchDegree fun(final PsiType actualType, final AopReferenceTarget holder) {
    return holder.accepts(actualType);
  }

}
