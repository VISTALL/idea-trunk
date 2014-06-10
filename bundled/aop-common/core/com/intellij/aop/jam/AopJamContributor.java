/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import static com.intellij.patterns.PsiJavaPatterns.*;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemRegistrar;

/**
 * @author peter
 */
public class AopJamContributor extends SemContributor {
  public void registerSemProviders(SemRegistrar registrar) {
    AopAspectImpl.ASPECT_META.register(registrar, psiClass().withAnnotation(AopConstants.ASPECT_ANNO).andNot(psiElement().compiled()));

    AopPointcutImpl.POINTCUT_METHOD_META.register(registrar, psiMethod().andNot(psiElement().compiled()).withAnnotation(AopConstants.POINTCUT_ANNO));
    
  }

}
