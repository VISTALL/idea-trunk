// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:field-result interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:field-result documentation</h3>
 * @Target({}) @Retention(RUNTIME)
 *         public @interface FieldResult {
 *           String name();
 *           String column();
 *         }
 * </pre>
 */
public interface FieldResult extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the column child.
	 * @return the value of the column child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getColumn();


}
