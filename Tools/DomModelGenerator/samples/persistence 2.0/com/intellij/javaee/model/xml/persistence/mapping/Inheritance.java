// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:inheritance interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:inheritance documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface Inheritance {
 *           InheritanceType strategy() default SINGLE_TABLE;
 *         }
 * </pre>
 */
public interface Inheritance extends JavaeeDomModelElement {

	/**
	 * Returns the value of the strategy child.
	 * @return the value of the strategy child.
	 */
	@NotNull
	GenericAttributeValue<InheritanceType> getStrategy();


}
