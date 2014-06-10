/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface RefBase extends DomElement {
  /**
	 * Returns the value of the bean child.
   * <pre>
   * <h3>Attribute null:bean documentation</h3>
   * 	The name of the referenced bean.
   *
   * </pre>
   * @return the value of the bean child.
   */
  @NotNull
  @Convert(value = SpringBeanResolveConverter.PropertyBean.class)
  GenericAttributeValue<SpringBeanPointer> getBean();

  /**
	 * Returns the value of the local child.
   * <pre>
   * <h3>Attribute null:local documentation</h3>
   * 	The name of the referenced bean. The value must be a bean ID,
   * 	and thus can be checked by the XML parser, thus should be preferred
   * 	for references within the same bean factory XML file.
   *
   * </pre>
   * @return the value of the local child.
   */
  @NotNull
  @Convert(value = SpringBeanResolveConverter.PropertyLocal.class)
  GenericAttributeValue<SpringBeanPointer> getLocal();
}
