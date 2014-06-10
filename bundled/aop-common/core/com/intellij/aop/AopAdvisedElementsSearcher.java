/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.psi.*;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class AopAdvisedElementsSearcher implements Processor<Processor<PsiClass>> {
  private final PsiManager myManager;

  protected AopAdvisedElementsSearcher(PsiManager manager) {
    myManager = manager;
  }

  public PsiManager getManager() {
    return myManager;
  }

  public abstract boolean process(final Processor<PsiClass> processor);

  public boolean shouldSuppressErrors() {
    return false;
  }

  public boolean acceptsBoundMethod(@NotNull final PsiMethod method) {
    if (method.isConstructor() || method.hasModifierProperty(PsiModifier.ABSTRACT)) return false;

    final PsiClass containingClass = method.getContainingClass();
    if (containingClass == null) return false;

    return !CommonClassNames.JAVA_LANG_OBJECT.equals(containingClass.getQualifiedName()) && isAcceptable(containingClass);
  }

  public boolean acceptsBoundMethodHeavy(@NotNull final PsiMethod method) {
    return true;
  }

  public boolean isAcceptable(final PsiClass psiClass) {
    return false;
  }

}
