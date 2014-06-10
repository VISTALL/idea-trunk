/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;

/**
 * @author peter
 */
@Convert(value = SpringBeanResolveConverter.PropertyBean.class)
public interface PNamespaceRefValue extends GenericAttributeValue<SpringBeanPointer>, SpringPropertyDefinition {
}