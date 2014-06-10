/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class PsiIfPointcutExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiIfPointcutExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiIfPointcutExpression";
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