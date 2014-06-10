// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.hibernate.model.enums.EventType;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-configuration-3.0.dtd:event interface.
 */
public interface Event extends CommonDomModelElement {

	/**
	 * Returns the value of the type child.
	 * Attribute type
	 * @return the value of the type child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<EventType> getType();


	/**
	 * Returns the list of listener children.
	 * @return the list of listener children.
	 */
	@NotNull
	List<Listener> getListeners();
	/**
	 * Adds new child to the list of listener children.
	 * @return created child
	 */
	Listener addListener();


}
