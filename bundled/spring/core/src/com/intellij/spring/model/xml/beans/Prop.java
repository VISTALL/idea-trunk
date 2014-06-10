// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.converters.PropertyKeyConverter;
import com.intellij.spring.model.values.PropsValueConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

@Convert(PropsValueConverter.class)
public interface Prop extends GenericDomValue<Object> {

  @NotNull
  @Required
  @Convert(PropertyKeyConverter.class)
  GenericAttributeValue<String> getKey();
}
