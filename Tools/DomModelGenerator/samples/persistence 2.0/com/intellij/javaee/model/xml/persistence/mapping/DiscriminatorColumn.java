// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:discriminator-column interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:discriminator-column documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface DiscriminatorColumn {
 *           String name() default "DTYPE";
 *           DiscriminatorType discriminatorType() default STRING;
 *           String columnDefinition() default "";
 *           int length() default 31;
 *         }
 * </pre>
 */
public interface DiscriminatorColumn extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the discriminator-type child.
	 * @return the value of the discriminator-type child.
	 */
	@NotNull
	GenericAttributeValue<DiscriminatorType> getDiscriminatorType();


	/**
	 * Returns the value of the column-definition child.
	 * @return the value of the column-definition child.
	 */
	@NotNull
	GenericAttributeValue<String> getColumnDefinition();


	/**
	 * Returns the value of the length child.
	 * @return the value of the length child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getLength();


}
