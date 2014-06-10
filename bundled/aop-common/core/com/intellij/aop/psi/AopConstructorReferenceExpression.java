/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;

/**
 * @author peter
 */
public class AopConstructorReferenceExpression extends AopMemberReferenceExpression {
  public AopConstructorReferenceExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopConstructorReferenceExpression";
  }

}
