// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:version interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:version documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface Version {}
 * </pre>
 */
public interface Version extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the column child.
	 * @return the value of the column child.
	 */
	@NotNull
	Column getColumn();


	/**
	 * Returns the value of the temporal child.
	 * @return the value of the temporal child.
	 */
	@NotNull
	GenericDomValue<Temporal> getTemporal();


}
