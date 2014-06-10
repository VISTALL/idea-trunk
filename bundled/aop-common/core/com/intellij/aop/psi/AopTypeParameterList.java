/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AopTypeParameterList extends AopAbstractList<PsiType> {

  public AopTypeParameterList(@NotNull final ASTNode node) {
    super(node);
  }

  protected PsiType getPsiType(@NotNull final PsiType psiType) {
    return psiType;
  }

  public String toString() {
    return "AopTypeParameterList";
  }

}