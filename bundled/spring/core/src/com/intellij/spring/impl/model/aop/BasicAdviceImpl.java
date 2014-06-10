/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.PointcutContext;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.psi.PsiMethod;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class BasicAdviceImpl extends BaseImpl implements BasicAdvice {

  @Nullable
  public PsiPointcutExpression getPointcutExpression() {
    final AopPointcut aopPointcut = getPointcutRef().getValue();
    return aopPointcut != null ? aopPointcut.getExpression().getValue() : getPointcut().getValue();
  }

  @NotNull
  public SpringAdvisedElementsSearcher getSearcher() {
    return new SpringAdvisedElementsSearcher(getPsiManager(), SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(this)));
  }

  @NotNull
  public AopAdviceType getAdviceType() {
    return AopAdviceType.valueOf(getXmlElementName().replace('-', '_').toUpperCase());
  }

  public PointcutMatchDegree accepts(final PsiMethod method) {
    final PsiPointcutExpression expression = getPointcutExpression();
    if (expression == null) return PointcutMatchDegree.FALSE;

    return expression.acceptsSubject(new PointcutContext(getMethod().getValue()), method);
  }


}
