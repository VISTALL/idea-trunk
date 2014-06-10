/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class PsiTypedPointcutExpression extends AopElementBase implements PsiPointcutExpression {
  public PsiTypedPointcutExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceHolder getTypeReference() {
    return (AopReferenceHolder)findChildByType(AopElementTypes.AOP_REFERENCE_HOLDER);
  }

}
