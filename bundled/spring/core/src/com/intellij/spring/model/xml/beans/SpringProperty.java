// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.spring.model.converters.BeanPropertyConverter;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.util.xml.*;

import java.util.List;

/**
 * http://www.springframework.org/schema/beans:propertyType interface.
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringProperty extends SpringInjection, SpringPropertyDefinition {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * 	The name of the property, following JavaBean naming conventions.
   * <p/>
   * </pre>
   *
   * @return the value of the name child.
   */
  @Required
  @Convert(BeanPropertyConverter.class)
  @NameValue(referencable = false)
  GenericAttributeValue<List<BeanProperty>> getName();
}
