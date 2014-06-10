// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:validated-byType interface.
 */
public interface ValidatedBy extends DomElement {

	/**
	 * Returns the value of the include-existing-validators child.
	 * @return the value of the include-existing-validators child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getIncludeExistingValidators();


	/**
	 * Returns the list of value children.
	 * @return the list of value children.
	 */
	@NotNull
	List<GenericDomValue<String>> getValues();
	/**
	 * Adds new child to the list of value children.
	 * @return created child
	 */
	GenericDomValue<String> addValue();


}
