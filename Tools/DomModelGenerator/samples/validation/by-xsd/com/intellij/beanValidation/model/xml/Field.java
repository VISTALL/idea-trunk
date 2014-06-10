// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:fieldType interface.
 */
public interface Field extends DomElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the ignore-annotations child.
	 * @return the value of the ignore-annotations child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getIgnoreAnnotations();


	/**
	 * Returns the list of constraint children.
	 * @return the list of constraint children.
	 */
	@NotNull
	List<Constraint> getConstraints();
	/**
	 * Adds new child to the list of constraint children.
	 * @return created child
	 */
	Constraint addConstraint();


}
