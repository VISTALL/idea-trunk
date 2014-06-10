/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;

//@see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeCustomInitMethod
public class SpringBeanInitMethodConverter extends SpringBeanMethodConverter {

  protected boolean checkModifiers(final PsiMethod method) {
    return !method.hasModifierProperty(PsiModifier.ABSTRACT);
  }
}