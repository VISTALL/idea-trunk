/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamMethodMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author peter
 */
public abstract class AopPointcutImpl implements JamElement, AopPointcut, PointcutContainer {
  private static final JamAnnotationMeta POINTCUT_META = new JamAnnotationMeta(AopConstants.POINTCUT_ANNO);
  public static final JamMethodMeta<AopPointcutImpl> POINTCUT_METHOD_META = new JamMethodMeta<AopPointcutImpl>(AopPointcutImpl.class);

  private static final JamStringAttributeMeta.Single<String> ARG_NAMES_META = JamAttributeMeta.singleString("argNames");

  public GenericValue<PsiPointcutExpression> getExpression() {
    final JamStringAttributeMeta.Single<PsiPointcutExpression> meta =
      JamAttributeMeta.singleString("value", new JamConverter<PsiPointcutExpression>() {
        @Override
        public PsiPointcutExpression fromString(@Nullable String s, JamStringAttributeElement<PsiPointcutExpression> context) {
          return getPointcutExpression(context.getPsiElement());
        }
      });
    return POINTCUT_META.getAttribute(getPsiElement(), meta);
  }

  public PsiElement getIdentifyingPsiElement() {
    final PsiAnnotation annotation = getAnnotation();
    return annotation == null ? getPsiElement() : annotation;
  }

  public JamStringAttributeElement<String> getArgNames() {
    return POINTCUT_META.getAttribute(getPsiElement(), ARG_NAMES_META);
  }

  public GenericValue<String> getQualifiedName() {
    return ReadOnlyGenericValue.getInstance(getPsiElement().getContainingClass().getQualifiedName() + "." + getPsiElement().getName());
  }

  public int getParameterCount() {
    return getPsiElement().getParameterList().getParametersCount();
  }

  @Nullable
  protected PsiPointcutExpression getPointcutExpression(@Nullable PsiAnnotationMemberValue value) {
    return getPsiPointcutExpression(value);
  }

  @Nullable
  public static PsiPointcutExpression getPsiPointcutExpression(@Nullable final PsiElement value) {
    assert value == null || value.isPhysical();

    if (value instanceof PsiBinaryExpression) {
      return getPsiPointcutExpression(((PsiBinaryExpression)value).getLOperand());
    }

    if (value instanceof PsiLanguageInjectionHost) {
      final List<Pair<PsiElement,TextRange>> list = ((PsiLanguageInjectionHost)value).getInjectedPsi();
      if (list != null) {
        Pair<PsiElement,TextRange> pair = ContainerUtil.find(list, new Condition<Pair<PsiElement, TextRange>>() {
          public boolean value(final Pair<PsiElement, TextRange> pair) {
            return pair.first instanceof AopPointcutExpressionFile;
          }
        });
        if (pair != null) {
          return ((AopPointcutExpressionFile)pair.first).getPointcutExpression();
        }
      }
    }
    return null;
  }

  @Nullable
  public PsiAnnotation getAnnotation() {
    return POINTCUT_META.getAnnotation(getPsiElement());
  }

  public PsiManager getPsiManager() {
    return getPsiElement().getManager();
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public boolean isValid() {
    return getPsiElement().isValid();
  }
}
