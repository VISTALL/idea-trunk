/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.jam.AopConstants;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class SpringArgNamesManipulator extends ArgNamesManipulator {
  private final XmlTag myTag;
  @NonNls private static final String ARG_NAMES = "arg-names";

  public SpringArgNamesManipulator(final XmlTag tag) {
    myTag = tag;
  }

  @Nullable
  public String getArgNames() {
    return myTag.getAttributeValue(ARG_NAMES);
  }

  public void setArgNames(@Nullable final String argNames) throws IncorrectOperationException {
    myTag.setAttribute(ARG_NAMES, argNames);
  }

  @NotNull
  public PsiElement getArgNamesProblemElement() {
    final XmlAttribute attribute = myTag.getAttribute(ARG_NAMES);
    if (attribute != null) {
      final XmlAttributeValue xmlAttributeValue = attribute.getValueElement();
      if (xmlAttributeValue != null && !xmlAttributeValue.getValueTextRange().isEmpty()) return xmlAttributeValue;
      return attribute.getFirstChild();
    }
    return getCommonProblemElement();
  }

  @NotNull
  public PsiElement getCommonProblemElement() {
    return ObjectUtils.assertNotNull(XmlTagUtil.getStartTagNameElement(myTag));
  }

  @NotNull
  @NonNls
  public String getArgNamesAttributeName() {
    return ARG_NAMES;
  }

  @Nullable
  public PsiReference getReturningReference() {
    return getParameterReference(AopConstants.RETURNING_PARAM, myTag);
  }

  @Nullable
  public PsiReference getThrowingReference() {
    return getParameterReference(AopConstants.THROWING_PARAM, myTag);
  }

  public AopAdviceType getAdviceType() {
    final DomElement element = DomManager.getDomManager(myTag.getProject()).getDomElement(myTag);
    return element instanceof AopAdvice ? ((AopAdvice)element).getAdviceType() : null;
  }

  private static PsiReference getParameterReference(final String qname, final XmlTag tag) {
    final XmlAttribute attribute = tag.getAttribute(qname);
    if (attribute != null) {
      final XmlAttributeValue element = attribute.getValueElement();
      if (element != null) {
        return element.getReference();
      }
    }
    return null;
  }


}
