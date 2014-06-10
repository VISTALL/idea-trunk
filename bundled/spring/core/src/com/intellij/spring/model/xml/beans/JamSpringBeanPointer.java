/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.jam.JamPsiMemberSpringBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class JamSpringBeanPointer extends SpringBaseBeanPointer {
  @NotNull private final JamPsiMemberSpringBean mySpringBean;

  protected JamSpringBeanPointer(@NotNull final JamPsiMemberSpringBean springBean) {
    super(springBean.getBeanName());
    mySpringBean = springBean;
  }

  @NotNull
  public JamPsiMemberSpringBean getSpringBean() {
    return mySpringBean;
  }

  public boolean isAbstract() {
    return false;
  }

  @Nullable
  public SpringBeanPointer getParentPointer() {
    return null;
  }

  @Nullable
  public PsiElement getPsiElement() {
    final JamPsiMemberSpringBean springBean = getSpringBean();

    return springBean.getIdentifyingPsiElement();
  }

  public SpringBeanPointer derive(@NotNull final String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(getBasePointer(), name);
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JamSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    final JamSpringBeanPointer that = (JamSpringBeanPointer)o;

    if (!mySpringBean.equals(that.mySpringBean)) return false;

    return true;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mySpringBean.hashCode();
    return result;
  }

  @Nullable
  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  public PsiManager getPsiManager() {
    return getSpringBean().getPsiManager();
  }

  public PsiFile getContainingFile() {
    return getSpringBean().getContainingFile();
  }

  public Icon getBeanIcon() {
    return SpringIcons.SPRING_JAVA_BEAN_ICON;
  }

  public boolean isValid() {
    return getSpringBean().isValid();
  }

}