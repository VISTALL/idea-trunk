// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:join-table interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:join-table documentation</h3>
 * @Target({METHOD, FIELD}) 
 * @Retention(RUNTIME)
 *         public @interface JoinTable {
 *           String name() default "";
 *           String catalog() default "";
 *           String schema() default "";
 *           JoinColumn[] joinColumns() default {};
 *           JoinColumn[] inverseJoinColumns() default {};
 *           UniqueConstraint[] uniqueConstraints() default {};
 *         }
 * </pre>
 */
public interface JoinTable extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	GenericAttributeValue<String> getName();


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
	 * Returns the list of join-column children.
	 * @return the list of join-column children.
	 */
	@NotNull
	List<JoinColumn> getJoinColumns();
	/**
	 * Adds new child to the list of join-column children.
	 * @return created child
	 */
	JoinColumn addJoinColumn();


	/**
	 * Returns the list of inverse-join-column children.
	 * @return the list of inverse-join-column children.
	 */
	@NotNull
	List<JoinColumn> getInverseJoinColumns();
	/**
	 * Adds new child to the list of inverse-join-column children.
	 * @return created child
	 */
	JoinColumn addInverseJoinColumn();


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
