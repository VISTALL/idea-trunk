/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiMember;
import com.intellij.lang.ASTNode;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class PsiControlFlowPointcutExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiControlFlowPointcutExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "PsiControlFlowPointcutExpression";
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