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
public class AopSubtypeExpression extends AopElementBase implements AopReferenceQualifier {
  public AopSubtypeExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopSubtypeExpression";
  }

  @NotNull
  public AopTypeExpression getSupertypeExpression() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return ContainerUtil.map2List(getSupertypeExpression().getPatterns(), new Function<AopPsiTypePattern, AopPsiTypePattern>() {
      public AopPsiTypePattern fun(final AopPsiTypePattern aopPsiTypePattern) {
        return new SubtypePattern(aopPsiTypePattern);
      }
    });
  }

  public String getTypePattern() {
    return "'_";
  }

  public AopReferenceExpression.Resolvability getResolvability() {
    return AopReferenceExpression.Resolvability.NONE;
  }

}