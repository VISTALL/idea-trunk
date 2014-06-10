// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:join-column interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:join-column documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface JoinColumn {
 *           String name() default "";
 *           String referencedColumnName() default "";
 *           boolean unique() default false;
 *           boolean nullable() default true;
 *           boolean insertable() default true;
 *           boolean updatable() default true;
 *           String columnDefinition() default "";
 *           String table() default "";
 *         }
 * </pre>
 */
public interface JoinColumn extends JavaeeDomModelElement {

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
	 * Returns the value of the unique child.
	 * @return the value of the unique child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUnique();


	/**
	 * Returns the value of the nullable child.
	 * @return the value of the nullable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getNullable();


	/**
	 * Returns the value of the insertable child.
	 * @return the value of the insertable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getInsertable();


	/**
	 * Returns the value of the updatable child.
	 * @return the value of the updatable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUpdatable();


	/**
	 * Returns the value of the column-definition child.
	 * @return the value of the column-definition child.
	 */
	@NotNull
	GenericAttributeValue<String> getColumnDefinition();


	/**
	 * Returns the value of the table child.
	 * @return the value of the table child.
	 */
	@NotNull
	GenericAttributeValue<String> getTable();


}
