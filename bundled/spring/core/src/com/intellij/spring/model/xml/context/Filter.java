// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/context:filterType interface.
 */
public interface Filter extends DomSpringBean, SpringContextElement {

	/**
	 * Returns the value of the type child.
	 * @return the value of the type child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<Type> getType();


	/**
	 * Returns the value of the expression child.
	 * @return the value of the expression child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getExpression();


}
