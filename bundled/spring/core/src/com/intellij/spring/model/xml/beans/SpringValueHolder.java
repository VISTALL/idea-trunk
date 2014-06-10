/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.values.PropertyValueConverter;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public interface SpringValueHolder extends SpringElementsHolder, SpringValueHolderDefinition {

  /**
   * Returns the value of the ref child.
   * <pre>
   * <h3>Attribute null:ref documentation</h3>
   * 	A short-cut alternative to a nested "<ref bean='...'/>".
   * <p/>
   * </pre>
   *
   * @return the value of the ref child.
   */
  @NotNull
  @Attribute(value = "ref")
  @Convert(value = SpringBeanResolveConverter.PropertyBean.class)
  GenericAttributeValue<SpringBeanPointer> getRefAttr();

  /**
   * Returns the value of the value child.
   * <pre>
   * <h3>Attribute null:value documentation</h3>
   * 	A short-cut alternative to a nested "<value>...</value>"
   * 	element.
   * <p/>
   * </pre>
   *
   * @return the value of the value child.
   */
  @NotNull
  @Attribute(value = "value")
  @Convert(PropertyValueConverter.class)
  GenericAttributeValue<String> getValueAttr();

}
