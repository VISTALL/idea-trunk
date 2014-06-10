// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:arg-typeElemType interface.
 */
public interface ArgType extends DomElement {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	@Required
	String getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(@NotNull String value);


	/**
	 * Returns the value of the match child.
	 * <pre>
	 * <h3>Attribute null:match documentation</h3>
	 * 	Specification of the type of an overloaded method argument as a String.
	 * 	For convenience, this may be a substring of the FQN. E.g. all the
	 * 	following would match "java.lang.String":
	 * 	- java.lang.String
	 * 	- String
	 * 	- Str
	 * 	As the number of arguments will be checked also, this convenience
	 * 	can often be used to save typing.
	 * 					
	 * </pre>
	 * @return the value of the match child.
	 */
	@NotNull
	GenericAttributeValue<String> getMatch();


}
