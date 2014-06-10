/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author peter
 */
public class PointcutContext {
  private final Map<String,AopReferenceTarget> myMap = new THashMap<String, AopReferenceTarget>();

  public PointcutContext() {
  }

  public PointcutContext(@Nullable PsiPointcutExpression expression) {
    this(expression == null ? null : expression.getContainingFile().getAopModel().getPointcutMethod());
  }

  public PointcutContext(@Nullable PsiMethod method) {
    if (method != null) {
      for (final PsiParameter parameter : method.getParameterList().getParameters()) {
        final String paramName = parameter.getName();
        if (paramName != null) {
          addParameter(paramName, new AopParameterReferenceTarget(parameter));
        }
      }
    }
  }

  private AopReferenceTarget getParameter(String paramName) {
    return myMap.get(paramName);
  }

  public void addParameter(@NotNull String paramName, AopReferenceTarget holder) {
    myMap.put(paramName, holder);
  }

  @NotNull
  public AopReferenceTarget resolve(@NotNull AopReferenceHolder pattern) {
    final AopTypeExpression typeExpression = pattern.getTypeExpression();
    if (typeExpression instanceof AopReferenceExpression) {
      final AopReferenceTarget target = getParameter(pattern.getText());
      if (target != null) {
        return target;
      }
    }
    return pattern;
  }

}
