/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate;

import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.xml.mapping.HbmQuery;
import com.intellij.hibernate.model.xml.mapping.HbmQueryParam;
import com.intellij.jpa.QueryReferencesUtil;
import static com.intellij.patterns.DomPatterns.domElement;
import static com.intellij.patterns.DomPatterns.withDom;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.patterns.XmlPatterns;
import static com.intellij.patterns.StandardPatterns.*;
import com.intellij.persistence.QueryInjectionSupport;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Gregory.Shrago
 */
public class HibernateQueryInjectionSupport implements QueryInjectionSupport {

  private static final ElementPattern[] QUERY_PATTERNS = new ElementPattern[] {
    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().withName("createQuery").definedInClass(HibernateConstants.SESSION_CLASS)),
    PlatformPatterns.psiElement(XmlText.class).withParent(XmlPatterns.xmlTag().withName("query").and(withDom(domElement(HbmQuery.class))))
  };

  private static final ElementPattern[] QUERY_NAME_PATTERNS = new ElementPattern[] {
    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().withName("getNamedQuery").definedInClass(HibernateConstants.SESSION_CLASS)),
  };

  @NonNls
  private static final Set<String> SET_PARAMETER_METHOD_NAMES = new THashSet<String>(Arrays.asList("setParameter", "setParameterList",
                                                                                                   "setProperties", "setString",
                                                                                                   "setCharacter", "setBoolean", "setByte",
                                                                                                   "setShort", "setInteger", "setLong",
                                                                                                   "setFloat", "setDouble", "setBinary",
                                                                                                   "setText", "setSerializable", "setLocale",
                                                                                                   "setBigDecimal", "setBigInteger",
                                                                                                   "setDate", "setTime", "setTimestamp",
                                                                                                   "setCalendar", "setCalendarDate",
                                                                                                   "setEntity"));

  private static final ElementPattern[] PARAMETER_NAME_PATTERNS = new ElementPattern[] {
    XmlPatterns.xmlAttributeValue(XmlPatterns.xmlAttribute("name").withParent(
      XmlPatterns.xmlTag().withName("query-param").withParent(XmlPatterns.xmlTag().withName("query")).and(withDom(domElement(HbmQueryParam.class))))),

    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().withName(string().oneOf(SET_PARAMETER_METHOD_NAMES)).definedInClass(HibernateConstants.QUERY_CLASS).
      withParameters(CommonClassNames.JAVA_LANG_STRING, "..")),
  };


  public boolean acceptsQueryInjection(PsiElement element) {
    return or(QUERY_PATTERNS).accepts(element);
  }

  public boolean acceptsQueryName(final PsiElement element) {
    return or(QUERY_NAME_PATTERNS).accepts(element);
  }

  public boolean acceptsParameterName(final PsiElement element) {
    return or(PARAMETER_NAME_PATTERNS).accepts(element);
  }

  public PsiElement getQueryReferenceOrElementByParameterReference(final PsiElement paramElement) {
    if (paramElement instanceof XmlAttributeValue) {
      final PsiElement tag = paramElement.getParent().getParent().getParent();
      if (tag instanceof XmlTag && "query".equals(((XmlTag)tag).getName())) {
        return tag;
      }
    }
    final PsiMethodCallExpression expression = PsiTreeUtil.getParentOfType(paramElement, PsiMethodCallExpression.class);
    return expression == null ? null : QueryReferencesUtil.skipChainedMethodCalls(
      expression.getMethodExpression().getQualifierExpression(), SET_PARAMETER_METHOD_NAMES);
  }

}
