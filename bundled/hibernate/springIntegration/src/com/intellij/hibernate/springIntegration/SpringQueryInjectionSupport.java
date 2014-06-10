/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.springIntegration;

import com.intellij.patterns.ElementPattern;
import static com.intellij.patterns.StandardPatterns.*;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.persistence.QueryInjectionSupport;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Query injection supported for:
 * <pre>
 * // Convenience finder methods for HQL strings
 *
 * HibernateOperations#find(String)
 * HibernateOperations#find(String,Object)
 * HibernateOperations#find(String,Object[])
 *
 * // Note: parameter name and JavaDoc for next method are misleading, see:
 * // http://opensource.atlassian.com/projects/spring/browse/SPR-3385 (fixed in 2.0.5)
 *
 * HibernateOperations#findByNamedParam(String,String,Object)
 * HibernateOperations#findByNamedParam(String,String[],Object[])
 * HibernateOperations#findByValueBean(String,Object)
 *
 * // Convenience query methods for iteration and bulk updates/deletes
 * HibernateOperations#iterate(String)
 * HibernateOperations#iterate(String,Object)
 * HibernateOperations#iterate(String,Object[])
 * HibernateOperations#bulkUpdate(String)
 * HibernateOperations#bulkUpdate(String,Object)
 * HibernateOperations#bulkUpdate(String,Object[])
 *
 * JpaTemplate / JPAQL:
 *
 * (org.springframework.orm.jpa.JpaOperations implemented by org.springframework.orm.jpa.JpaTemplate)
 *
 * First parameter in signatures is JPAQL queryString:
  *
 * // Convenience finder methods
 * JpaOperations#find(String)
 * JpaOperations#find(String,Object...)
 * JpaOperations#findByNamedParams(String,Map<String,? extends Object>)
 * </pre>
 *
 *
 * Query Names supported in:
 *
 * <pre>
 *  Developers using spring often code to the JpaTemplate/HibernateTemplate classes, which provide transparent transaction participation, exception translation and other convenience features. It would be very nice if query/param name resolving would be available for those APIs as well.
 * For examples, see http://static.springframework.org/spring/docs/2.0.x/reference/orm.html
 *
 * Here's a complete list of methods taking query/parameter names. Collected from spring 2.0.3 release.
 * JPA:
 *
 * (org.springframework.orm.jpa.JpaOperations implemented by org.springframework.orm.jpa.JpaTemplate)
 * First parameter in signature is name of "Named query"
 *
 * JpaOperations#findByNamedQuery(String)
 * JpaOperations#findByNamedQuery(String,Object...)
 * JpaOperations#findByNamedQueryAndNamedParams(String,Map<String,? extends Object>)
 *
 * Hibernate:
 *
 * (org.springframework.orm.hibernate3.HibernateOperations implemented by org.springframework.orm.hibernate3.HibernateTemplate)
 * First parameter in signature is name of "Named query"
 *
 * HibernateOperations#findByNamedQuery(String)
 * HibernateOperations#findByNamedQuery(String,Object)
 * HibernateOperations#findByNamedQuery(String,Object[])
 * HibernateOperations#findByNamedQueryAndNamedParam(String,String,Object) // second parameter is query param name
 * HibernateOperations#findByNamedQueryAndNamedParam(String,String[],Object[]) // second parameter is query param name array
 * HibernateOperations#findByNamedQueryAndValueBean(String,Object)
 *
 * For the findByNamedQueryAndNamedParam methods, it might be possible to provide parameter name completion as well - at least for the method that takes a single param/value pair.*
 *
 * </pre>
 *
 *
 * @author Gregory.Shrago
 */
public class SpringQueryInjectionSupport implements QueryInjectionSupport {

  public static final String HIBERNATE_OPERATIONS_CLASS = "org.springframework.orm.hibernate3.HibernateOperations";
  public static final String JPA_OPERATIONS_CLASS = "org.springframework.orm.jpa.JpaOperations";

  private static final ElementPattern[] QUERY_PATTERNS = new ElementPattern[] {
    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().definedInClass(HIBERNATE_OPERATIONS_CLASS).withName(
      string().oneOf("find", "findByNamedParam", "findByValueBean", "iterate", "bulkUpdate"))),
    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().definedInClass(JPA_OPERATIONS_CLASS).withName(
      string().oneOf("find", "findByNamedParams"))),
  };

  private static final ElementPattern[] QUERY_NAME_PATTERNS = new ElementPattern[]{
    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().definedInClass(JPA_OPERATIONS_CLASS).withName(
      string().oneOf("findByNamedQuery", "findByNamedQueryAndNamedParams"))),

    PsiJavaPatterns.psiExpression().methodCallParameter(0, PsiJavaPatterns.psiMethod().definedInClass(HIBERNATE_OPERATIONS_CLASS).withName(
      string().oneOf("findByNamedQuery", "findByNamedQueryAndNamedParam", "findByNamedQueryAndValueBean"))),
  };

  private static final ElementPattern[] PARAM_NAME_PATTERNS = new ElementPattern[]{
    PsiJavaPatterns.psiExpression().methodCallParameter(1, PsiJavaPatterns.psiMethod().definedInClass(HIBERNATE_OPERATIONS_CLASS).withName(
      string().oneOf("findByNamedQueryAndNamedParam", "findByNamedParam"))),
  };

  public boolean acceptsQueryInjection(final PsiElement element) {
    return or (QUERY_PATTERNS).accepts(element);
  }

  public boolean acceptsQueryName(final PsiElement element) {
    return or(QUERY_NAME_PATTERNS).accepts(element);
  }

  public boolean acceptsParameterName(final PsiElement element) {
    return or (PARAM_NAME_PATTERNS).accepts(element);
  }

  public PsiExpression getQueryReferenceOrElementByParameterReference(final PsiElement paramElement) {
    final PsiMethodCallExpression expression = PsiTreeUtil.getParentOfType(paramElement, PsiMethodCallExpression.class);
    final PsiExpression[] psiExpressions = expression == null? PsiExpression.EMPTY_ARRAY : expression.getArgumentList().getExpressions();
    return psiExpressions.length == 0? null : psiExpressions[0];
  }

}
