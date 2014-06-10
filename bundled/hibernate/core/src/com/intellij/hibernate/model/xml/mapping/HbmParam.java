// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.ParamNameConverter;
import com.intellij.util.xml.*;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:param interface.
 */
public interface HbmParam<T> extends CommonDomModelElement, GenericDomValue<T> {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	T getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(T value);


	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
        @NameValue
        @NotNull
	@Required
        @Convert(ParamNameConverter.class)
        GenericAttributeValue<String> getName();


}
