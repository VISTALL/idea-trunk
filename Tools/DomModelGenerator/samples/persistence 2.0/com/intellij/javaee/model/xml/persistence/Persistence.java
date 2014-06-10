// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence

package com.intellij.javaee.model.xml.persistence;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence:persistenceElemType interface.
 */
public interface Persistence extends JavaeeDomModelElement {

	/**
	 * Returns the value of the version child.
	 * @return the value of the version child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getVersion();


	/**
	 * Returns the list of persistence-unit children.
	 * @return the list of persistence-unit children.
	 */
	@NotNull
	@Required
	List<PersistenceUnit> getPersistenceUnits();
	/**
	 * Adds new child to the list of persistence-unit children.
	 * @return created child
	 */
	PersistenceUnit addPersistenceUnit();


}
