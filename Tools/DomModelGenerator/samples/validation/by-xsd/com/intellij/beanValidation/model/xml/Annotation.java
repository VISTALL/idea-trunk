// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:annotationType interface.
 */
public interface Annotation extends DomElement {

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
