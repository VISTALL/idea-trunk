/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopAnnotationExpression extends AopElementBase implements AopAnnotationPattern{
  public AopAnnotationExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopAnnotationExpression";
  }

  @Nullable
  public AopReferenceHolder getAnnotationPattern() {
    return findChildByClass(AopReferenceHolder.class);
  }

  public final Collection<AopPsiTypePattern> getPatterns() {
    final AopReferenceHolder holder = getAnnotationPattern();
    return holder == null ? Collections.<AopPsiTypePattern>emptyList() : holder.getPatterns();
  }


}