/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class GenericPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myErasure;
  private final AopPsiTypePattern[] myParameters;

  public GenericPattern(final AopPsiTypePattern erasure, final AopPsiTypePattern... parameters) {
    myErasure = erasure;
    myParameters = parameters;
  }

  public AopPsiTypePattern getErasure() {
    return myErasure;
  }

  public AopPsiTypePattern[] getParameters() {
    return myParameters;
  }

  public boolean accepts(@NotNull final PsiType type) {
    return accepts(type, false);
  }

  private boolean accepts(@NotNull final PsiType type, boolean allowWildcardAssignability) {
    if (type instanceof PsiClassType) {
      if (!myErasure.accepts(type)) return false;

      final PsiClassType classType = (PsiClassType)type;
      if (classType.isRaw()) return allowWildcardAssignability;

      final PsiType[] parameters = classType.getParameters();
      if (myParameters.length != parameters.length) return false;

      for (int i = 0; i < parameters.length; i++) {
        final AopPsiTypePattern paramPattern = myParameters[i];
        final PsiType parameter = parameters[i];
        if (!(allowWildcardAssignability && paramPattern instanceof WildcardPattern ? paramPattern.canBeAssignableFrom(parameter) == PointcutMatchDegree.TRUE : paramPattern.accepts(parameter))) return false;
      }

      return true;
    }

    return false;
  }

  @NotNull
  public PointcutMatchDegree canBeAssignableFrom(@NotNull PsiType type) {
    if (accepts(type, true)) return PointcutMatchDegree.TRUE;
    boolean maybe = false;
    for (final PsiType psiType : type.getSuperTypes()) {
      final PointcutMatchDegree degree = canBeAssignableFrom(psiType);
      if (degree == PointcutMatchDegree.TRUE) return degree;
      maybe = degree == PointcutMatchDegree.MAYBE;
    }
    return maybe ? PointcutMatchDegree.MAYBE : PointcutMatchDegree.FALSE;
  }
  
}
