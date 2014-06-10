/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.PNamespaceRefValue;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * @author peter
 */
public abstract class PNamespaceRefValueImpl implements PNamespaceRefValue {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    return PNamespaceValueImpl.getPropertyType(this, getPropertyName());
  }

  @NotNull
  @NonNls
  public String getPropertyName() {
    final String name = getXmlElementName();
    return name.substring(0, name.length() - "-ref".length());
  }

  @Nullable
  public PsiType[] getTypesByValue() {
    return null;
  }

  @NotNull
  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return this;
  }

  @NotNull
  public GenericDomValue<?> getValueElement() {
    return getParent().getGenericInfo().getAttributeChildDescription(getPropertyName()).getDomAttributeValue(getParent());
  }
}