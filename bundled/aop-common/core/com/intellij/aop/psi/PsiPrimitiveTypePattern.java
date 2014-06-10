/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class PsiPrimitiveTypePattern extends AopPsiTypePattern {
  private final PsiType myType;

  public PsiPrimitiveTypePattern(@NotNull PsiType type) {
    myType = type;
  }

  public boolean accepts(@NotNull final PsiType type) {
    return type.equals(myType);
  }

  @NotNull
  public PsiType getType() {
    return myType;
  }

}
