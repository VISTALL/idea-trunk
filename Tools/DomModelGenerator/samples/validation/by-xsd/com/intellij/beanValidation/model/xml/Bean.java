// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:beanType interface.
 */
public interface Bean extends DomElement {

	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClassAttr();


	/**
	 * Returns the value of the ignore-annotations child.
	 * @return the value of the ignore-annotations child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getIgnoreAnnotations();


	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.SubTag ("class")
	Class getClazz();


	/**
	 * Returns the list of field children.
	 * @return the list of field children.
	 */
	@NotNull
	List<Field> getFields();
	/**
	 * Adds new child to the list of field children.
	 * @return created child
	 */
	Field addField();


	/**
	 * Returns the list of getter children.
	 * @return the list of getter children.
	 */
	@NotNull
	List<Getter> getGetters();
	/**
	 * Adds new child to the list of getter children.
	 * @return created child
	 */
	Getter addGetter();


}
