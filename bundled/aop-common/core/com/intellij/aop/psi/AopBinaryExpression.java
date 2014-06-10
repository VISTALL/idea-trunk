/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopBinaryExpression extends AopElementBase implements PsiPointcutExpression, AopTypeExpression {

  public AopBinaryExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public final Pair<AopPatternContainer,AopPatternContainer> getOperands() {
    final AopPatternContainer[] containers = findChildrenByClass(AopPatternContainer.class);
    if (containers.length != 2) return null;
    return Pair.create(containers[0], containers[1]);
  }

  @NotNull
  public AopOperation getOperation() {
    return findChildByType(AopElementTypes.AOP_OR) != null ? AopOperation.OR : AopOperation.AND;
  }

  @Nullable
  public PsiElement getOpToken() {
    final PsiElement or = findChildByType(AopElementTypes.AOP_OR);
    return or != null ? or : findChildByType(AopElementTypes.AOP_AND);
  }

  public String toString() {
    return "AopBinaryExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    final Pair<AopPatternContainer, AopPatternContainer> pair = getOperands();
    if (pair == null || !(pair.first instanceof PsiPointcutExpression)) return PointcutMatchDegree.FALSE;

    final PsiPointcutExpression leftOperand = (PsiPointcutExpression)pair.first;
    final PsiPointcutExpression rightOperand = (PsiPointcutExpression)pair.second;

    final boolean and = getOperation() == AopOperation.AND;
    PointcutMatchDegree leftResult = leftOperand.acceptsSubject(context, member);
    if (and && leftResult == PointcutMatchDegree.FALSE) {
      return PointcutMatchDegree.FALSE;
    }
    else if (!and && leftResult == PointcutMatchDegree.TRUE) {
      return PointcutMatchDegree.TRUE;
    }

    PointcutMatchDegree rightResult = rightOperand.acceptsSubject(context, member);
    return and ? PointcutMatchDegree.and(leftResult, rightResult) : PointcutMatchDegree.or(leftResult, rightResult);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final Pair<AopPatternContainer, AopPatternContainer> pair = getOperands();
    if (pair == null) return Collections.emptyList();

    final Collection<AopPsiTypePattern> leftPatterns = pair.first.getPatterns();
    final Collection<AopPsiTypePattern> rightPatterns = pair.second.getPatterns();
    final Collection<AopPsiTypePattern> result = new THashSet<AopPsiTypePattern>();
    if (getOperation() == AopOperation.AND) {
      conjunctPatterns(leftPatterns, rightPatterns, result);
    } else {
      result.addAll(leftPatterns);
      result.addAll(rightPatterns);
    }
    return result;
  }

  public String getTypePattern() {
    final AopTypeExpression[] expressions = findChildrenByClass(AopTypeExpression.class);
    if (expressions.length != 2) return null;

    final String pattern0 = expressions[0].getTypePattern();
    final String pattern1 = expressions[1].getTypePattern();
    if (pattern0 == null || pattern1 == null) return null;

    final Pair<AopPatternContainer, AopPatternContainer> pair = getOperands();
    if (pair == null) return null;

    return "'_:[is(\"" + pattern0 + "\") " + (getOperation() == AopOperation.AND ? "&&" : "||") + " is(\"" + pattern1 + "\")]";
  }

  public static void conjunctPatterns(final Collection<AopPsiTypePattern> leftPatterns,
                                     final Collection<AopPsiTypePattern> rightPatterns,
                                     final Collection<AopPsiTypePattern> result) {
    for (final AopPsiTypePattern leftPattern : leftPatterns) {
      for (final AopPsiTypePattern rightPattern : rightPatterns) {
        result.add(AopPsiTypePatternsUtil.conjunctPatterns(leftPattern, rightPattern));
      }
    }
  }

  public enum AopOperation {
    AND, OR
  }
}
