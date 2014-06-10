/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author peter
 */
public abstract class MethodPatternPointcut extends AopElementBase implements PsiPointcutExpression {
  public MethodPatternPointcut(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceHolder getReturnType() {
    return findChildByClass(AopReferenceHolder.class);
  }

  @Nullable
  public AopModifierList getModifierList() {
    return findChildByClass(AopModifierList.class);
  }

  @Nullable
  public AopMemberReferenceExpression getMethodReference() {
    return findChildByClass(AopMemberReferenceExpression.class);
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @Nullable
  public AopThrowsList getThrowsList() {
    return findChildByClass(AopThrowsList.class);
  }

  @Nullable
  public AopAnnotationHolder getAnnotationHolder() {
    return findChildByClass(AopAnnotationHolder.class);
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    final AopMemberReferenceExpression methodReference = getMethodReference();
    if (methodReference == null) return Arrays.asList(AopPsiTypePattern.FALSE);
    return methodReference.getQualifierPatterns();
  }
}
