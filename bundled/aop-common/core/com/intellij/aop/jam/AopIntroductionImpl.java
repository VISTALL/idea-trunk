/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.jam;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.psi.AopReferenceHolder;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.aop.psi.PsiTargetExpression;
import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.JamClassAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.annotations.JamAnnotation;
import com.intellij.jam.annotations.JamAttribute;
import com.intellij.jam.reflect.*;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class AopIntroductionImpl implements AopIntroduction, JamElement {
  private static final JamStringAttributeMeta.Single<AopReferenceHolder> VALUE_META =
    JamAttributeMeta.singleString("value", new JamConverter<AopReferenceHolder>() {
    @Override
    public AopReferenceHolder fromString(@Nullable String s, JamStringAttributeElement<AopReferenceHolder> context) {
      return getTypesMatchingPattern(context.getPsiElement());
    }
  });

  public static final JamAnnotationMeta DECLARE_PARENTS_META = new JamAnnotationMeta(AopConstants.DECLARE_PARENTS_ANNO)
    .addAttribute(VALUE_META);


  @NotNull
  @JamAnnotation(AopConstants.DECLARE_PARENTS_ANNO)
  @JamAttribute(AopConstants.DEFAULT_IMPL_PARAM)
  public abstract JamClassAttributeElement getDefaultImpl();

  public XmlTag getXmlTag() {
    return null;
  }

  public Module getModule() {
    return null;
  }

  public PsiAnnotation getIdentifyingPsiElement() {
    return DECLARE_PARENTS_META.getAnnotation(getPsiElement());
  }

  public PsiFile getContainingFile() {
    return getPsiElement().getContainingFile();
  }

  @NotNull
  public GenericValue<PsiClass> getImplementInterface() {
    return new ReadOnlyGenericValue<PsiClass>() {
      public PsiClass getValue() {
        final PsiType type = getPsiElement().getType();
        return type instanceof PsiClassType ? ((PsiClassType)type).resolve() : null;
      }

      public String getStringValue() {
        return getPsiElement().getType().getCanonicalText();
      }
    };
  }

  @NotNull
  public GenericValue<AopReferenceHolder> getTypesMatching() {
    return DECLARE_PARENTS_META.getAttribute(getPsiElement(), VALUE_META);
  }

  @Nullable
  public static AopReferenceHolder getTypesMatchingPattern(@Nullable final PsiElement value) {
    final PsiPointcutExpression expression = AopPointcutImpl.getPsiPointcutExpression(value);
    return expression instanceof PsiTargetExpression ? ((PsiTargetExpression)expression).getTypeReference() : null;
  }

  public PsiManager getPsiManager() {
    return getPsiElement().getManager();
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiField getPsiElement();

  public boolean isValid() {
    return getPsiElement().isValid();
  }
}
