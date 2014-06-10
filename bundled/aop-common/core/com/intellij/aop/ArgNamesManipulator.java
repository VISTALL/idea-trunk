/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiParameter;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class ArgNamesManipulator {
  @Nullable
  public abstract String getArgNames();
  public abstract void setArgNames(@Nullable String argNames) throws IncorrectOperationException;

  @NotNull
  public abstract PsiElement getArgNamesProblemElement();

  @NotNull
  public abstract PsiElement getCommonProblemElement();

  @NotNull @NonNls
  public abstract String getArgNamesAttributeName();

  @Nullable
  public abstract PsiReference getReturningReference();

  @Nullable
  public PsiParameter getReturningParameter() {
    final PsiReference psiReference = getReturningReference();
    return psiReference == null ? null : (PsiParameter)psiReference.resolve();
  }

  @Nullable
  public abstract PsiReference getThrowingReference();

  @Nullable
  public PsiParameter getThrowingParameter() {
    final PsiReference psiReference = getThrowingReference();
    return psiReference == null ? null : (PsiParameter)psiReference.resolve();
  }

  @Nullable
  public abstract AopAdviceType getAdviceType();
}
