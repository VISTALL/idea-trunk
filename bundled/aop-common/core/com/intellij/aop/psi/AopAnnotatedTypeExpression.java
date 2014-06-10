/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopAnnotatedTypeExpression extends AopElementBase implements AopTypeExpression{
  public AopAnnotatedTypeExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopAnnotatedTypeExpression";
  }

  @Nullable
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Nullable
  public AopAnnotationHolder getAnnotationHolder() {
    return findChildByClass(AopAnnotationHolder.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return getPatterns(getTypeExpression());
  }

  public String getTypePattern() {
    return "'_";
  }

  @NotNull
  public Collection<AopPsiTypePattern> getQualifierPatterns() {
    final AopTypeExpression expression = getTypeExpression();
    if (!(expression instanceof AopReferenceExpression)) return Collections.emptyList();

    return getPatterns(((AopReferenceExpression)expression).getGeneralizedQualifier());
  }

  private Collection<AopPsiTypePattern> getPatterns(final AopTypeExpression typeExpression) {
    if (typeExpression == null) return Collections.emptyList();

    final AopAnnotationHolder annotationHolder = getAnnotationHolder();
    if (annotationHolder == null) return Collections.emptyList();

    final Collection<AopPsiTypePattern> typePatterns = typeExpression.getPatterns();
    final Collection<AopPsiTypePattern> annoPatterns = annotationHolder.getPatterns();
    final THashSet<AopPsiTypePattern> result = new THashSet<AopPsiTypePattern>();
    AopBinaryExpression.conjunctPatterns(typePatterns, annoPatterns, result);
    return result;
  }

}