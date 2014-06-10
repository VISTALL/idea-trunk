/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopParenthesizedExpression extends AopElementBase implements PsiPointcutExpression, AopTypeExpression, AopReferenceQualifier{
  public AopParenthesizedExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopParenthesizedExpression";
  }

  @Nullable
  public PsiPointcutExpression getInnerPointcutExpression() {
    return findChildByClass(PsiPointcutExpression.class);
  }

  @Nullable
  public AopTypeExpression getInnerTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    final PsiPointcutExpression pointcutExpression = getInnerPointcutExpression();
    return pointcutExpression != null ? pointcutExpression.acceptsSubject(context, member) : PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final AopTypeExpression typeExpression = getInnerTypeExpression();
    if (typeExpression != null) return typeExpression.getPatterns();
    final PsiPointcutExpression pointcutExpression = getInnerPointcutExpression();
    if (pointcutExpression != null) return pointcutExpression.getPatterns();
    return Collections.emptyList();
  }

  public String getTypePattern() {
    final AopTypeExpression expression = getInnerTypeExpression();
    if (expression != null) {
      final String pattern = expression.getTypePattern();
      if (pattern == null) return null;

      return "'_:[is(\"" + pattern + "\")]";
    }
    return null;
  }

  public AopReferenceExpression.Resolvability getResolvability() {
    return AopReferenceExpression.Resolvability.NONE;
  }
}