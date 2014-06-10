// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:constraintType interface.
 */
public interface Constraint extends DomElement {

	/**
	 * Returns the value of the annotation child.
	 * @return the value of the annotation child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getAnnotation();


	/**
	 * Returns the value of the message child.
	 * @return the value of the message child.
	 */
	@NotNull
	GenericDomValue<String> getMessage();


	/**
	 * Returns the value of the groups child.
	 * @return the value of the groups child.
	 */
	@NotNull
	Groups getGroups();


	/**
	 * Returns the list of element children.
	 * @return the list of element children.
	 */
	@NotNull
	List<Element> getElements();
	/**
	 * Adds new child to the list of element children.
	 * @return created child
	 */
	Element addElement();


}
