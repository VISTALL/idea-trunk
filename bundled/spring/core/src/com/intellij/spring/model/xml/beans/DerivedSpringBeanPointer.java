/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.spring.model.xml.CommonSpringBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author peter
 */
public class DerivedSpringBeanPointer extends SpringBeanPointer{
  private final String myName;
  private final SpringBaseBeanPointer myBasePointer;

  public DerivedSpringBeanPointer(@NotNull final SpringBaseBeanPointer basePointer, @NotNull final String name) {
    myBasePointer = basePointer;
    myName = name;
  }

  public SpringBeanPointer derive(@NotNull final String name) {
    if (name.equals(myName)) return this;
    if (name.equals(myBasePointer.getName())) return myBasePointer;
    return new DerivedSpringBeanPointer(myBasePointer, name);
  }

  public String[] getAliases() {
    return myBasePointer.getAliases();
  }

  @NotNull
  public SpringBaseBeanPointer getBasePointer() {
    return myBasePointer;
  }

  public boolean isValid() {
    return myBasePointer.isValid();
  }

  @Nullable
  public PsiClass getBeanClass() {
    return myBasePointer.getBeanClass();
  }

  public Icon getBeanIcon() {
    return myBasePointer.getBeanIcon();
  }

  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  public PsiClass[] getEffectiveBeanType() {
    return myBasePointer.getEffectiveBeanType();
  }

  @Nullable
  public String getName() {
    return myName;
  }

  @Nullable
  public SpringBeanPointer getParentPointer() {
    return myBasePointer.getParentPointer();
  }

  @Nullable
  public PsiElement getPsiElement() {
    return myBasePointer.getPsiElement();
  }

  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  @NotNull
  public CommonSpringBean getSpringBean() {
    return myBasePointer.getSpringBean();
  }

  public boolean isAbstract() {
    return myBasePointer.isAbstract();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DerivedSpringBeanPointer)) return false;

    final DerivedSpringBeanPointer that = (DerivedSpringBeanPointer)o;

    if (!myBasePointer.equals(that.myBasePointer)) return false;
    if (!myName.equals(that.myName)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myName.hashCode();
    result = 31 * result + myBasePointer.hashCode();
    return result;
  }
}
