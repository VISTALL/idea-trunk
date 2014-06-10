// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:generated-value interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:generated-value documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface GeneratedValue {
 *           GenerationType strategy() default AUTO;
 *           String generator() default "";
 *         }
 * </pre>
 */
public interface GeneratedValue extends JavaeeDomModelElement {

	/**
	 * Returns the value of the strategy child.
	 * @return the value of the strategy child.
	 */
	@NotNull
	GenericAttributeValue<GenerationType> getStrategy();


	/**
	 * Returns the value of the generator child.
	 * @return the value of the generator child.
	 */
	@NotNull
	GenericAttributeValue<String> getGenerator();


}
