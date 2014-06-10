/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.jam;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.psi.PointcutContext;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.jam.JamChief;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class AopAdviceImpl implements JamChief, AopAdvice, PointcutContainer {
  private final AopAdviceType myType;
  protected final JamAnnotationMeta myAnnoMeta;


  public AopAdviceImpl(final AopAdviceType type, JamAnnotationMeta annoMeta) {
    myAnnoMeta = annoMeta;
    myType = type;
  }

  @Nullable
  protected PsiAnnotationMemberValue getAnnoParam() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.VALUE_ATTR).getPsiElement();
  }

  public JamStringAttributeElement<String> getArgNames() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.ARG_NAMES_ATTR);
  }

  @Nullable
  public PsiPointcutExpression getPointcutExpression() {
    final PsiAnnotationMemberValue param = getAnnoParam();
    return AopPointcutImpl.getPsiPointcutExpression(param);
  }

  @NotNull
  public AopAdviceType getAdviceType() {
    return myType;
  }

  public AopAdvisedElementsSearcher getSearcher() {
    final PsiPointcutExpression expression = getPointcutExpression();
    return expression == null ? null : expression.getContainingFile().getAopModel().getAdvisedElementsSearcher();
  }

  public PointcutMatchDegree accepts(final PsiMethod method) {
    final PsiPointcutExpression expression = getPointcutExpression();
    return expression == null ? PointcutMatchDegree.FALSE : expression
      .acceptsSubject(new PointcutContext(expression), method);
  }

  public XmlTag getXmlTag() {
    return null;
  }

  public Module getModule() {
    return null;
  }

  public PsiAnnotation getIdentifyingPsiElement() {
    return myAnnoMeta.getAnnotation(getPsiElement());
  }

  public PsiFile getContainingFile() {
    return getPsiElement().getContainingFile();
  }

  @Nullable
  public static PsiParameter findParameter(@Nullable final PsiMethod method, @NotNull final String parameterName) {
    if (method != null) {
      final PsiParameter[] parameters = method.getParameterList().getParameters();
      for (final PsiParameter parameter : parameters) {
        if (parameterName.equals(parameter.getName())) return parameter;
      }
    }
    return null;
  }

  @Nullable
  public PsiAnnotation getAnnotation() {
    return getIdentifyingPsiElement();
  }

  public PsiManager getPsiManager() {
    return getPsiElement().getManager();
  }

  public boolean isValid() {
    return getPsiElement().isValid();
  }

  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public abstract static class Before extends AopAdviceImpl {
    public Before() {
      super(AopAdviceType.BEFORE, AopAdviceMetas.BEFORE_META);
    }
  }
  abstract public static class After extends AopAdviceImpl {
    public After() {
      super(AopAdviceType.AFTER, AopAdviceMetas.AFTER_META);
    }
  }
  public abstract static class Around extends AopAdviceImpl {
    public Around() {
      super(AopAdviceType.AROUND, AopAdviceMetas.AROUND_META);
    }
  }

}