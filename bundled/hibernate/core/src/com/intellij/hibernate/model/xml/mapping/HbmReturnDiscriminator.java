// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:return-discriminator interface.
 */
public interface HbmReturnDiscriminator extends CommonDomModelElement {

	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getColumn();


}
