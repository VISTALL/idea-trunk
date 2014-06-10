/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiStaticInitializationExpression extends PsiTypedPointcutExpression {

  public PsiStaticInitializationExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiStaticInitializationExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.FALSE);
  }

}