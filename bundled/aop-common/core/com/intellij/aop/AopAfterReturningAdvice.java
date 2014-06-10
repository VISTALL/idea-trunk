/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.util.xml.GenericValue;
import com.intellij.psi.PsiParameter;

/**
 * @author peter
 */
public interface AopAfterReturningAdvice extends AopAdvice{
  GenericValue<PsiParameter> getReturning();
}
