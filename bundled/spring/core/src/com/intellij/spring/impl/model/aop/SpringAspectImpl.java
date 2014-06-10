/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.aop;

import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class SpringAspectImpl implements SpringAspect {
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  @Nullable
  public PsiClass getPsiClass() {
    final SpringBeanPointer pointer = getRef().getValue();
    return pointer != null ? pointer.getBeanClass() : null;
  }

}