/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author peter
 */
public class AopGenericTypeExpression extends AopElementBase implements AopTypeExpression{
  public AopGenericTypeExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopParameterizedTypeExpression";
  }

  @NotNull
  public AopTypeExpression getRawTypeReference() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @NotNull
  public AopTypeParameterList getTypeParameterList() {
    return findNotNullChildByClass(AopTypeParameterList.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final Collection<AopPsiTypePattern> erasurePatterns = getRawTypeReference().getPatterns();
    final PsiElement[] parameters = getTypeParameterList().getParameters();
    final AopPsiTypePattern[][] parameterPatterns = new AopPsiTypePattern[parameters.length][];
    for (int i = 0; i < parameters.length; i++) {
      final AopTypeExpression expression = ((AopReferenceHolder)parameters[i]).getTypeExpression();
      if (expression == null) return Collections.emptyList();
      final Collection<AopPsiTypePattern> patterns = expression.getPatterns();
      parameterPatterns[i] = patterns.toArray(new AopPsiTypePattern[patterns.size()]);
    }
    final Set<AopPsiTypePattern> result = new THashSet<AopPsiTypePattern>();
    for (final AopPsiTypePattern erasurePattern : erasurePatterns) {
      final int[] indices = new int[parameterPatterns.length];
      while (true) {
        final AopPsiTypePattern[] paramVariant = new AopPsiTypePattern[parameterPatterns.length];
        for (int i = 0; i < paramVariant.length; i++) {
          paramVariant[i] = parameterPatterns[i][indices[i]];
        }
        result.add(new GenericPattern(erasurePattern, paramVariant));

        int j = indices.length - 1;
        while (j >= 0 && indices[j] == parameterPatterns[j].length - 1) j--;
        if (j < 0) break;
        indices[j]++;
        while (++j < indices.length) indices[j] = 0;
      }
    }
    return result;
  }

  public String getTypePattern() {
    return "'_";
  }


}