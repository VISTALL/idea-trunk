// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:embedded-id interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:embedded-id documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface EmbeddedId {}
 * </pre>
 */
public interface EmbeddedId extends JavaeeDomModelElement {

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
	 * Returns the list of attribute-override children.
	 * @return the list of attribute-override children.
	 */
	@NotNull
	List<AttributeOverride> getAttributeOverrides();
	/**
	 * Adds new child to the list of attribute-override children.
	 * @return created child
	 */
	AttributeOverride addAttributeOverride();


}
