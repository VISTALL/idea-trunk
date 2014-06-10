// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.converters.ParentRefConverter;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:refElemType interface.
 */
public interface SpringRef extends RefBase {


  /**
   * Returns the value of the parent child.
   * <pre>
   * <h3>Attribute null:parent documentation</h3>
   * 	The name of the referenced bean *in a parent factory*.
   * <p/>
   * </pre>
   *
   * @return the value of the parent child.
   */
  @NotNull
  @Attribute("parent")
  @Convert(value = ParentRefConverter.class)
  GenericAttributeValue<SpringBeanPointer> getParentAttr();


}
