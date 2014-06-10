/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.properties;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.ReferenceSetBase;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class PropertyReferenceSet extends ReferenceSetBase<PropertyReference> {

  @Nullable private final PsiClass myBeanClass;

  private final GenericDomValue<List<BeanProperty>> myGenericDomValue;
  private final ConvertContext myContext;
  private final CommonSpringBean myBean;

  public PropertyReferenceSet(@NotNull PsiElement element,
                              @Nullable PsiClass beanClass,
                              @NotNull GenericDomValue<List<BeanProperty>> genericDomValue,
                              ConvertContext context,
                              CommonSpringBean bean) {
    super(element);
    myBeanClass = beanClass;
    myGenericDomValue = genericDomValue;
    myContext = context;
    myBean = bean;
  }

  @NotNull
  protected PropertyReference createReference(final TextRange range, final int index) {
    return new PropertyReference(this, range, index);
  }

  public PropertyReference[] getPsiReferences() {
    return getReferences().toArray(new PropertyReference[getReferences().size()]);
  }

  public GenericDomValue<List<BeanProperty>> getGenericDomValue() {
    return myGenericDomValue;
  }

  @Nullable
  public PsiClass getBeanClass() {
    return myBeanClass;
  }


  public ConvertContext getContext() {
    return myContext;
  }

  public CommonSpringBean getBean() {
    return myBean;
  }
}
