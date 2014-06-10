/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.lang;

import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.converters.BeanPropertyConverter;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;

import java.util.List;

/**
 * @author peter
 */
@Namespace(SpringConstants.LANG_NAMESPACE_KEY)
public interface LangProperty extends SpringProperty {

  @Convert(value = BeanPropertyConverter.class, soft = true)
  GenericAttributeValue<List<BeanProperty>> getName();
}
