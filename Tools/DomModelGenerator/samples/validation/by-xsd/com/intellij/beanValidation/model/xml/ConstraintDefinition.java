// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:constraint-definitionType interface.
 */
public interface ConstraintDefinition extends DomElement {

	/**
	 * Returns the value of the annotation child.
	 * @return the value of the annotation child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getAnnotation();


	/**
	 * Returns the value of the validated-by child.
	 * @return the value of the validated-by child.
	 */
	@NotNull
	@Required
	ValidatedBy getValidatedBy();


}
