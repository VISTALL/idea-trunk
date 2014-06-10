/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author peter
 */
public class PsiPointcutReferenceExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiPointcutReferenceExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceExpression getReferenceExpression() {
    return findChildByClass(AopReferenceExpression.class);
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  public String toString() {
    return "PsiPointcutReferenceExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      final AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        final PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.acceptsSubject(createContext(context, pointcutExpression), member);
        }
      }
    }
    return PointcutMatchDegree.FALSE;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceExpression expression = getReferenceExpression();
    if (expression != null) {
      final AopPointcut pointcut = expression.resolvePointcut();
      if (pointcut != null) {
        final PsiPointcutExpression pointcutExpression = pointcut.getExpression().getValue();
        if (pointcutExpression != null) {
          return pointcutExpression.getPatterns();
        }
      }
    }
    return Arrays.asList(AopPsiTypePattern.FALSE);
  }

  private PointcutContext createContext(final PointcutContext context, final PsiPointcutExpression pointcutExpression) {
    final PsiMethod pointcutMethod = pointcutExpression.getContainingFile().getAopModel().getPointcutMethod();
    final PointcutContext newContext = new PointcutContext(pointcutMethod);
    if (pointcutMethod != null) {
      final PsiParameterList javaList = pointcutMethod.getParameterList();
      final AopParameterList aopList = getParameterList();
      if (aopList != null) {
        final PsiElement[] aopParameters = aopList.getParameters();
        final PsiParameter[] psiParameters = javaList.getParameters();
        if (javaList.getParametersCount() == aopParameters.length) {
          for (int i = 0; i < psiParameters.length; i++) {
            final PsiElement aopParameter = aopParameters[i];
            if (aopParameter instanceof AopReferenceHolder) {
              newContext.addParameter(psiParameters[i].getName(), context.resolve((AopReferenceHolder)aopParameter));
            }
          }
        }
      }
    }
    return newContext;
  }
}
