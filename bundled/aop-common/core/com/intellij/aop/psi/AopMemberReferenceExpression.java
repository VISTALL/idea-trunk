/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class AopMemberReferenceExpression extends AopElementBase {
  public AopMemberReferenceExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceExpression getReferenceExpression() {
    final AopTypeExpression aopTypeExpression = getTypeExpression();
    if (aopTypeExpression instanceof AopReferenceExpression) {
      return (AopReferenceExpression)aopTypeExpression;
    }
    if (aopTypeExpression instanceof AopAnnotatedTypeExpression) {
      final AopTypeExpression expression1 = ((AopAnnotatedTypeExpression)aopTypeExpression).getTypeExpression();
      if (expression1 instanceof AopReferenceExpression) {
        return (AopReferenceExpression)expression1;
      }
    }
    return null;
  }

  @Nullable
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  public Collection<AopPsiTypePattern> getQualifierPatterns() {
    final AopReferenceExpression expression = getReferenceExpression();
    if (expression == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    final AopReferenceQualifier qualifier = expression.getGeneralizedQualifier();
    if (qualifier == null) return Arrays.asList(AopPsiTypePattern.TRUE);

    final AopTypeExpression typeExpression = getTypeExpression();

    return typeExpression instanceof AopAnnotatedTypeExpression ? ((AopAnnotatedTypeExpression) typeExpression).getQualifierPatterns() : qualifier.getPatterns();
  }

  public Collection<AopPsiTypePattern> getPatterns() {
    final AopReferenceExpression expression = getReferenceExpression();
    final Collection<AopPsiTypePattern> patterns = getQualifierPatterns();
    if (expression != null && expression.isDoubleDot()) {
      return ContainerUtil.map(patterns, new Function<AopPsiTypePattern, AopPsiTypePattern>() {
        public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern) {
          return new ConcatenationPattern(aopPsiTypePattern, AopPsiTypePattern.TRUE, true);
        }
      });
    }
    return patterns;
  }

  public String toString() {
    return "AopMemberReferenceExpression";
  }

}