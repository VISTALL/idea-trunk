/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.aop;

import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.aop.psi.AopPointcutExpressionFile;
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
public class PointcutExpressionConverter extends Converter<PsiPointcutExpression> {
  public PsiPointcutExpression fromString(@Nullable @NonNls String s, final ConvertContext context) {
    final XmlAttributeValue attributeValue = ((GenericAttributeValue)context.getInvocationElement()).getXmlAttributeValue();
    if (attributeValue == null) return null;

    final List<Pair<PsiElement,TextRange>> list = ((PsiLanguageInjectionHost)attributeValue).getInjectedPsi();
    if (list == null || list.isEmpty()) return null;

    return ((AopPointcutExpressionFile)list.get(0).first).getPointcutExpression();
  }

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return null;
  }

  public String toString(@Nullable PsiPointcutExpression psiPointcutExpression, final ConvertContext context) {
    throw new UnsupportedOperationException("Method toString is not yet implemented in " + getClass().getName());
  }
}
