/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AopElementBase extends ASTWrapperPsiElement {
  public AopElementBase(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  public AopPointcutExpressionFile getContainingFile() {
    return (AopPointcutExpressionFile)super.getContainingFile();
  }
}
