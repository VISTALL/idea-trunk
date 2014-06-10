/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.MetadataPropertyValueConverter;
import com.intellij.spring.model.xml.beans.MetadataValue;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class MetadataValueImpl implements MetadataValue {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    return Collections.singletonList(((MetadataPropertyValueConverter)getConverter()).getRequiredType());
  }

  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return null;
  }

  public GenericDomValue<?> getValueElement() {
    return (GenericDomValue)this;
  }
}