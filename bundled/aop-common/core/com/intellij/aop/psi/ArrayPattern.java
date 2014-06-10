/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiEllipsisType;

/**
 * @author peter
 */
public class ArrayPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myComponentPattern;
  private final boolean myVarargs;

  public ArrayPattern(final AopPsiTypePattern componentPattern, final boolean isVarargs) {
    myComponentPattern = componentPattern;
    myVarargs = isVarargs;
  }

  public boolean isVarargs() {
    return myVarargs;
  }

  public boolean accepts(@NotNull final PsiType type) {
    return type instanceof PsiArrayType && myVarargs == type instanceof PsiEllipsisType &&
           myComponentPattern.accepts(((PsiArrayType)type).getComponentType());
  }
}
