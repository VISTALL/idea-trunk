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
 * http://jboss.org/xml/ns/javax/validation/mapping:elementType interface.
 */
public interface Element extends DomElement {

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
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


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


	/**
	 * Returns the list of annotation children.
	 * @return the list of annotation children.
	 */
	@NotNull
	List<Annotation> getAnnotations();
	/**
	 * Adds new child to the list of annotation children.
	 * @return created child
	 */
	Annotation addAnnotation();


}
