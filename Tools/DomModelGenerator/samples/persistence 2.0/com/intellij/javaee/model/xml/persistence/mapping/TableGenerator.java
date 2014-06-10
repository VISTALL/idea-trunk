// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:table-generator interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:table-generator documentation</h3>
 * @Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface TableGenerator {
 *           String name();
 *           String table() default "";
 *           String catalog() default "";
 *           String schema() default "";
 *           String pkColumnName() default "";
 *           String valueColumnName() default "";
 *           String pkColumnValue() default "";
 *           int initialValue() default 0;
 *           int allocationSize() default 50;
 *           UniqueConstraint[] uniqueConstraints() default {};
 *         }
 * </pre>
 */
public interface TableGenerator extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the table child.
	 * @return the value of the table child.
	 */
	@NotNull
	GenericAttributeValue<String> getTable();


	/**
	 * Returns the value of the catalog child.
	 * @return the value of the catalog child.
	 */
	@NotNull
	GenericAttributeValue<String> getCatalog();


	/**
	 * Returns the value of the schema child.
	 * @return the value of the schema child.
	 */
	@NotNull
	GenericAttributeValue<String> getSchema();


	/**
	 * Returns the value of the pk-column-name child.
	 * @return the value of the pk-column-name child.
	 */
	@NotNull
	GenericAttributeValue<String> getPkColumnName();


	/**
	 * Returns the value of the value-column-name child.
	 * @return the value of the value-column-name child.
	 */
	@NotNull
	GenericAttributeValue<String> getValueColumnName();


	/**
	 * Returns the value of the pk-column-value child.
	 * @return the value of the pk-column-value child.
	 */
	@NotNull
	GenericAttributeValue<String> getPkColumnValue();


	/**
	 * Returns the value of the initial-value child.
	 * @return the value of the initial-value child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getInitialValue();


	/**
	 * Returns the value of the allocation-size child.
	 * @return the value of the allocation-size child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getAllocationSize();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the list of unique-constraint children.
	 * @return the list of unique-constraint children.
	 */
	@NotNull
	List<UniqueConstraint> getUniqueConstraints();
	/**
	 * Adds new child to the list of unique-constraint children.
	 * @return created child
	 */
	UniqueConstraint addUniqueConstraint();


}
