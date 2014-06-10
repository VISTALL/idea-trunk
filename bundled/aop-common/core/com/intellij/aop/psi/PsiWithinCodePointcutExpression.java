/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiMember;
import com.intellij.lang.ASTNode;

/**
 * @author peter
 */
public class PsiWithinCodePointcutExpression extends MethodPatternPointcut{

  public PsiWithinCodePointcutExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @Override
  public String toString() {
    return "PsiWithinCodePointcutExpression";
  }
}