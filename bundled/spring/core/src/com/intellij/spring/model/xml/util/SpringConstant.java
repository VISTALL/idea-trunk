// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/util

package com.intellij.spring.model.xml.util;

import com.intellij.spring.model.values.converters.FieldRetrievingFactoryBeanConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/util:constantElemType interface.
 */
public interface SpringConstant extends SpringUtilElement, DomSpringBean {

	/**
	 * Returns the value of the static-field child.
	 * @return the value of the static-field child.
	 */
	@NotNull
	@Required
        @Referencing(value = FieldRetrievingFactoryBeanConverter.class) 
        GenericAttributeValue<String> getStaticField();


}
