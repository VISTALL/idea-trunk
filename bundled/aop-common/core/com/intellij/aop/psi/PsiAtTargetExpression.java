/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author peter
 */
public class PsiAtTargetExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtTargetExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtTargetExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PsiAtArgsExpression.canHaveAnnotation(member.getContainingClass(), getTypeReference(), context, PointcutMatchDegree.TRUE, PointcutMatchDegree.FALSE);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}