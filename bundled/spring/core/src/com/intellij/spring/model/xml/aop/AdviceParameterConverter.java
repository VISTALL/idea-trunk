/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.aop;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.aop.jam.AopAdviceImpl;
import com.intellij.aop.AopBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AdviceParameterConverter extends ResolvingConverter<PsiParameter> {
  @NotNull
  public Collection<? extends PsiParameter> getVariants(final ConvertContext context) {
    final BasicAdvice advice = (BasicAdvice)context.getInvocationElement().getParent();
    final PsiMethod method = advice.getMethod().getValue();
    if (method != null) {
      return Arrays.asList(method.getParameterList().getParameters());

    }
    return Collections.emptyList();
  }

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return AopBundle.message("error.cannot.resolve.parameter", s);
  }

  public PsiParameter fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return s == null ? null : AopAdviceImpl.findParameter(((BasicAdvice)context.getInvocationElement().getParent()).getMethod().getValue(), s);
  }

  public String toString(@Nullable PsiParameter psiParameter, final ConvertContext context) {
    return psiParameter == null ? null : psiParameter.getName();
  }
}
