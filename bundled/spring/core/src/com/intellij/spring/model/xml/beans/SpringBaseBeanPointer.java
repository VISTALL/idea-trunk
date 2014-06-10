/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.beans;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.SpringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author peter
 */
public abstract class SpringBaseBeanPointer extends SpringBeanPointer{
  private final String myName;
  private final NotNullLazyValue<String[]> myAliases = new NotNullLazyValue<String[]>() {
    @NotNull
    protected String[] compute() {
      return getSpringBean().getAliases();
    }
  };
  private final NotNullLazyValue<PsiClass[]> myEffectiveType = new NotNullLazyValue<PsiClass[]>() {
    @NotNull
    protected PsiClass[] compute() {
      return SpringUtils.getEffectiveBeanTypes(getSpringBean());
    }
  };

  protected SpringBaseBeanPointer(@Nullable final String name) {
    myName = name;
  }

  @Nullable
  public String getName() {
    return myName;
  }

  public PsiClass[] getEffectiveBeanType() {
    return myEffectiveType.getValue();
  }

  public Icon getBeanIcon() {
    return SpringIcons.SPRING_BEAN_ICON;
  }

  public String[] getAliases() {
    return myAliases.getValue();
  }

  @NotNull
  public SpringBaseBeanPointer getBasePointer() {
    return this;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof SpringBaseBeanPointer)) return false;

    final SpringBaseBeanPointer that = (SpringBaseBeanPointer)o;
    return Comparing.equal(getPsiElement(), that.getPsiElement());
  }

  public int hashCode() {
    return (myName != null ? myName.hashCode() : 0);
  }

}
