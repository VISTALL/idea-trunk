/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public interface AopReferenceTarget {
  @NotNull
  String getQualifiedName();

  PointcutMatchDegree canBeInstance(final PsiClass psiClass, final boolean allowPatterns);

  PointcutMatchDegree accepts(final PsiType actualType);

  @Nullable
  String getTypePattern();

  @Nullable PsiClass findClass();

  boolean isAssignableFrom(PsiType type);
}
