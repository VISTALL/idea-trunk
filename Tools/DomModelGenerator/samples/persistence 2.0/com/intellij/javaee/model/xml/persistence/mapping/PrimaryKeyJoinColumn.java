// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:primary-key-join-column interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:primary-key-join-column documentation</h3>
 * @Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface PrimaryKeyJoinColumn {
 *           String name() default "";
 *           String referencedColumnName() default "";
 *           String columnDefinition() default "";
 *         }
 * </pre>
 */
public interface PrimaryKeyJoinColumn extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the referenced-column-name child.
	 * @return the value of the referenced-column-name child.
	 */
	@NotNull
	GenericAttributeValue<String> getReferencedColumnName();


	/**
	 * Returns the value of the column-definition child.
	 * @return the value of the column-definition child.
	 */
	@NotNull
	GenericAttributeValue<String> getColumnDefinition();


}
