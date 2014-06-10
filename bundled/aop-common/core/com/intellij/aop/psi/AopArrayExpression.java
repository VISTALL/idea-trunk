/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author peter
 */
public class AopArrayExpression extends AopElementBase implements AopTypeExpression{
  public AopArrayExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopArrayExpression";
  }

  @NotNull
  public AopTypeExpression getTypeReference() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final boolean varargs = isVarargs();
    return ContainerUtil.map2List(getTypeReference().getPatterns(), new Function<AopPsiTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern) {
        return new ArrayPattern(aopPsiTypePattern, varargs);
      }
    });
  }

  public String getTypePattern() {
    final String pattern = getTypeReference().getTypePattern();
    return pattern == null ? null : isVarargs() ? pattern + "..." : pattern + "[]";
  }

  public boolean isVarargs() {
    return findChildByType(AopElementTypes.AOP_VARARGS) != null;
  }
}
