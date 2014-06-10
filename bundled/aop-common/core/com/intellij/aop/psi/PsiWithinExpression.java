/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiWithinExpression extends PsiTypedPointcutExpression {

  public PsiWithinExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiWithinExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    final AopReferenceHolder holder = getTypeReference();
    if (holder == null) return PointcutMatchDegree.FALSE;

    PsiClass psiClass = member.getContainingClass();
    PointcutMatchDegree degree = PointcutMatchDegree.FALSE;
    while (psiClass != null) {
      degree = PointcutMatchDegree.or(degree, holder.accepts(JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass)));
      psiClass = psiClass.getContainingClass();
    }
    return degree;

  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}
