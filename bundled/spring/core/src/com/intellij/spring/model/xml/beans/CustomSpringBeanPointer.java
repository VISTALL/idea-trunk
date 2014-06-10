/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.spring.model.xml.CustomBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author peter
 */
public class CustomSpringBeanPointer extends SpringBaseBeanPointer {
  private final DomSpringBeanPointer myBasePointer;
  private final int myIndex;

  private CustomSpringBeanPointer(@NotNull final CustomBeanWrapper wrapper, CustomBean bean, int index) {
    super(bean.getBeanName());
    myIndex = index;
    myBasePointer = DomSpringBeanPointer.createDomSpringBeanPointer(wrapper);
  }

  @NotNull
  public CustomBean getSpringBean() {
    return ((CustomBeanWrapper)myBasePointer.getSpringBean()).getCustomBeans().get(myIndex);
  }

  public boolean isValid() {
    if (!myBasePointer.isValid()) return false;

    final DomSpringBean baseBean = myBasePointer.getSpringBean();
    if (!(baseBean instanceof CustomBeanWrapper)) return false;

    final List<CustomBean> beans = ((CustomBeanWrapper)baseBean).getCustomBeans();
    if (beans.size() <= myIndex) return false;

    return true;
  }

  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  public static CustomSpringBeanPointer createCustomSpringBeanPointer(final CustomBean bean) {
    final CustomBeanWrapper wrapper = bean.getWrapper();
    final int index = wrapper.getCustomBeans().indexOf(bean);
    assert index >= 0;
    return new CustomSpringBeanPointer(wrapper, bean, index);
  }

  public boolean isAbstract() {
    return false;
  }

  public SpringBeanPointer getParentPointer() {
    return null;
  }

  public PsiElement getPsiElement() {
    return getSpringBean().getIdentifyingPsiElement();
  }

  public SpringBeanPointer derive(@NotNull final String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(this, name);
  }

  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof CustomSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    final CustomSpringBeanPointer that = (CustomSpringBeanPointer)o;

    if (myIndex != that.myIndex) return false;
    if (myBasePointer != null ? !myBasePointer.equals(that.myBasePointer) : that.myBasePointer != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (myBasePointer != null ? myBasePointer.hashCode() : 0);
    result = 31 * result + myIndex;
    return result;
  }
}