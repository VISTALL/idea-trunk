// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence

package com.intellij.javaee.model.xml.persistence;

import com.intellij.javaee.model.JavaeeDomModelElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence:propertiesElemType interface.
 */
public interface Properties extends JavaeeDomModelElement {

	/**
	 * Returns the list of property children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:property documentation</h3>
	 * A name-value pair.
	 * </pre>
	 * @return the list of property children.
	 */
	@NotNull
	List<Property> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	Property addProperty();


}
