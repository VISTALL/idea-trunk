/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class AopPointcutElementType extends AopElementType{
  public AopPointcutElementType(@NotNull @NonNls String debugName) {
    super(debugName);
  }

  public abstract PsiPointcutExpression createPsi(ASTNode node);
}
