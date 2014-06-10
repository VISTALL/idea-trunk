/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.model.xml.beans.PNamespaceValue;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.NullableFunction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Collections;

/**
 * @author peter
 */
public abstract class PNamespaceValueImpl implements PNamespaceValue {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    return getPropertyType(this, getPropertyName());
  }

  @NotNull
  static List<PsiType> getPropertyType(final DomElement value, @NotNull @NonNls final String name) {
    final SpringBean bean = (SpringBean)value.getParent();
    assert bean != null;
    final PsiClass beanClass = bean.getBeanClass();
    if (beanClass != null) {
      final List<PsiMethod> methods = PropertyUtil.getSetters(beanClass, name);
      return ContainerUtil.map2List(methods, new NullableFunction<PsiMethod, PsiType>() {
        public PsiType fun(final PsiMethod psiMethod) {
          return PropertyUtil.getPropertyType(psiMethod);
        }
      });
    }
    return Collections.emptyList();
  }

  @NotNull
  @NonNls
  public String getPropertyName() {
    return getXmlElementName();
  }

  @Nullable
  public PsiType[] getTypesByValue() {
    return null;
  }

  @NotNull
  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return getParent().getGenericInfo().getAttributeChildDescription(getPropertyName() + "-ref").getDomAttributeValue(getParent());
  }

  @NotNull
  public GenericDomValue<?> getValueElement() {
    return this;
  }
}
