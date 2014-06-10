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
public class AopWildcardExpression extends AopElementBase implements AopTypeExpression{
  public AopWildcardExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopWildcardExpression";
  }

  @Nullable
  public AopTypeExpression getBound() {
    return findChildByClass(AopTypeExpression.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final AopTypeExpression bound = getBound();
    final boolean isSuper = isSuper();
    if (bound == null) return Arrays.<AopPsiTypePattern>asList(new WildcardPattern(null, isSuper));

    return ContainerUtil.map2List(bound.getPatterns(), new Function<AopPsiTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern) {
        return new WildcardPattern(aopPsiTypePattern, isSuper);
      }
    });
  }

  public String getTypePattern() {
    return "'_";
  }

  public boolean isExtends() {
    return findChildByType(AopElementTypes.AOP_EXTENDS) != null;
  }

  public boolean isSuper() {
    return findChildByType(AopElementTypes.AOP_SUPER) != null;
  }
}