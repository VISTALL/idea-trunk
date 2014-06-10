/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * @author peter
 */
@Convert(MetadataPropertyValueConverter.class)
public interface MetadataValue extends GenericAttributeValue<Object>, SpringValueHolderDefinition {
}