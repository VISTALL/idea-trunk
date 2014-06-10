// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/util

package com.intellij.spring.model.xml.util;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/util:property-pathElemType interface.
 */
public interface PropertyPath extends SpringUtilElement, DomSpringBean {

	/**
	 * Returns the value of the path child.
	 * @return the value of the path child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getPath();
}
