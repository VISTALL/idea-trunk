/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.jam.*;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class JavaArgNamesManipulator extends ArgNamesManipulator {
  @NotNull private final PointcutContainer myContainer;

  public JavaArgNamesManipulator(@NotNull final PointcutContainer advice) {
    myContainer = advice;
  }

  @Nullable
  public String getArgNames() {
    return myContainer.getArgNames().getStringValue();
  }

  @NotNull
  public PsiElement getArgNamesProblemElement() {
    final PsiAnnotationMemberValue value = myContainer.getArgNames().getPsiElement();
    return value == null ? getCommonProblemElement() : value;
  }

  @NotNull
  public PsiElement getCommonProblemElement() {
    return myContainer.getAnnotation().getNameReferenceElement();
  }

  @Override
  public PsiParameter getReturningParameter() {
    return myContainer instanceof AopAfterReturningAdviceImpl ? ((AopAfterReturningAdviceImpl)myContainer).getReturning().getValue() : null;

  }

  @Override
  public PsiParameter getThrowingParameter() {
    return myContainer instanceof AopAfterThrowingAdviceImpl ? ((AopAfterThrowingAdviceImpl)myContainer).getThrowing().getValue() : null;
  }

  @NotNull
  @NonNls
  public String getArgNamesAttributeName() {
    return AopConstants.ARG_NAMES_PARAM;
  }

  @Nullable
  public PsiReference getReturningReference() {
    if (!(myContainer instanceof AopAfterReturningAdviceImpl)) return null;

    final JamStringAttributeElement<PsiParameter> returning = ((AopAfterReturningAdviceImpl)myContainer).getReturning();
    final PsiReference[] references = returning.getConverter().createReferences(returning);
    return references.length == 0 ? null : references[0];
  }

  @Nullable
  public PsiReference getThrowingReference() {
    if (!(myContainer instanceof AopAfterThrowingAdviceImpl)) return null;

    final JamStringAttributeElement<PsiParameter> throwing = ((AopAfterThrowingAdviceImpl)myContainer).getThrowing();
    final PsiReference[] references = throwing.getConverter().createReferences(throwing);
    return references.length == 0 ? null : references[0];

  }

  public AopAdviceType getAdviceType() {
    return myContainer instanceof AopAdvice ? ((AopAdvice)myContainer).getAdviceType() : null;
  }

  public void setArgNames(@Nullable final String argNames) throws IncorrectOperationException {
    myContainer.getArgNames().setStringValue(argNames);
  }
}
