/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;

//@see org.springframework.beans.factory.support.DisposableBeanAdapter#invokeCustomDestroyMethod
public class SpringBeanDestroyMethodConverter extends SpringBeanMethodConverter {

  protected boolean checkParameterList(final PsiMethod method) {
    final PsiParameterList parameterList = method.getParameterList();

    return parameterList.getParametersCount() == 0 ||
           (parameterList.getParametersCount() == 1 && PsiType.BOOLEAN.equals(parameterList.getParameters()[0].getType()));
  }

  protected boolean checkModifiers(final PsiMethod method) {
    return !method.hasModifierProperty(PsiModifier.ABSTRACT);
  }
}