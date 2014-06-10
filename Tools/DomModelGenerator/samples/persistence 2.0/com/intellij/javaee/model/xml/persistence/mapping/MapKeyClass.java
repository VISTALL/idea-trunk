// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:map-key-class interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:map-key-class documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface MapKeyClass {
 *           Class value();
 *         }
 * </pre>
 */
public interface MapKeyClass extends JavaeeDomModelElement {

	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClazz();


}
