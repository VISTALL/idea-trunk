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
public class PsiAtAnnotationExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator{

  public PsiAtAnnotationExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiAtAnnotationExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    final AopReferenceHolder pattern = getTypeReference();
    if (pattern == null) return PointcutMatchDegree.FALSE;

    return PointcutMatchDegree.valueOf(member.getModifierList().findAnnotation(context.resolve(pattern).getQualifiedName()) != null);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}