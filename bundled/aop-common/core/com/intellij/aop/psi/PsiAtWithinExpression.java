/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author peter
 */
public class PsiAtWithinExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtWithinExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtWithinExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    //todo this is only Spring-specific!
    return PsiAtArgsExpression.canHaveAnnotation(member.getContainingClass(), getTypeReference(), context, PointcutMatchDegree.TRUE, PointcutMatchDegree.FALSE);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}