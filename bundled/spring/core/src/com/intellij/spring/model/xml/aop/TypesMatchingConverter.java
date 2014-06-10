/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopReferenceHolder;
import com.intellij.aop.jam.AopIntroductionImpl;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * @author peter
 */
public class TypesMatchingConverter extends Converter<AopReferenceHolder> {
  public AopReferenceHolder fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return AopIntroductionImpl.getTypesMatchingPattern(((GenericAttributeValue)context.getInvocationElement()).getXmlAttributeValue());
  }

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return null;
  }

  public String toString(@Nullable AopReferenceHolder psiPointcutExpression, final ConvertContext context) {
    throw new UnsupportedOperationException("Method toString is not yet implemented in " + getClass().getName());
  }
}