/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.PropertyTypeResolvingConverter;
import com.intellij.psi.PsiType;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * @author Gregory.Shrago
 */
public interface HbmTypeHolderBase {
  @Convert(PropertyTypeResolvingConverter.class)
  @com.intellij.util.xml.Attribute("type")
  GenericAttributeValue<PsiType> getTypeAttr();

  HbmType getType();
}