// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/lang

package com.intellij.spring.model.xml.lang;

import com.intellij.util.xml.converters.values.ClassArrayConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/lang:dynamicScriptType interface.
 */
public interface DynamicScript extends DomElement, SimpleScript {

	/**
	 * Returns the value of the script-interfaces child.
	 * <pre>
	 * <h3>Attribute null:script-interfaces documentation</h3>
	 * 	The Java interfaces that the dynamic language-backed object is to expose; comma-delimited.
	 * 						
	 * </pre>
	 * @return the value of the script-interfaces child.
	 */
	@NotNull
	@Required
  @Referencing(value = ClassArrayConverter.class)
  GenericAttributeValue<String> getScriptInterfaces();

}
