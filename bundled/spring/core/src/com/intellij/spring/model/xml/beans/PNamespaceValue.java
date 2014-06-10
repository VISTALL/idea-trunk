/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.values.PropertyValueConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
@Convert(PropertyValueConverter.class)
public interface PNamespaceValue extends GenericAttributeValue<String>, SpringPropertyDefinition {

}
