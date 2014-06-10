// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/util

package com.intellij.spring.model.xml.util;

import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.ScopedElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/util:propertiesElemType interface.
 */
public interface UtilProperties extends SpringUtilElement, DomSpringBean, ScopedElement {
  String BEAN_CLASS_NAME = "org.springframework.beans.factory.config.PropertiesFactoryBean";

  /**
   * Returns the value of the location child.
   *
   * @return the value of the location child.
   */
  @NotNull
  @Required
  @Referencing(value = ResourceValueConverter.class)
  GenericAttributeValue<String> getLocation();
}
