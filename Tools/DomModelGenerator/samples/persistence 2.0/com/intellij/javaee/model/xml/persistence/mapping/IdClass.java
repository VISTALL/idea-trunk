// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:id-class interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:id-class documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface IdClass {
 *           Class value();
 *         }
 * </pre>
 */
public interface IdClass extends JavaeeDomModelElement {

	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClazz();


}
