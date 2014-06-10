/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
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
public class FieldPatternPointcut extends AopElementBase implements PsiPointcutExpression {
  public FieldPatternPointcut(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "FieldPatternPointcut";
  }

  @Nullable
  public AopModifierList getModifierList() {
    return findChildByClass(AopModifierList.class);
  }

  @Nullable
  public AopAnnotationHolder getAnnotationHolder() {
    return findChildByClass(AopAnnotationHolder.class);
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }
}