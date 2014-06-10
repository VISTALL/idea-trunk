// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.converters.AliasNameConverter;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:aliasElemType interface.
 */
public interface Alias extends DomElement {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * 	The name of the bean to define an alias for.
   * <p/>
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  @Required
  @Attribute(value = "name")
  @Convert(value = SpringBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAliasedBean();


  /**
   * Returns the value of the alias child.
   * <pre>
   * <h3>Attribute null:alias documentation</h3>
   * 	The alias name to define for the bean.
   * <p/>
   * </pre>
   *
   * @return the value of the alias child.
   */
  @NotNull
  @Required
  @Referencing(value = AliasNameConverter.class)  
  GenericAttributeValue<String> getAlias();


}
