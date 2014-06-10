// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:many-to-one interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:many-to-one documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface ManyToOne {
 *           Class targetEntity() default void.class;
 *           CascadeType[] cascade() default {};
 *           FetchType fetch() default EAGER;
 *           boolean optional() default true;
 *         }
 * </pre>
 */
public interface ManyToOne extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the target-entity child.
	 * @return the value of the target-entity child.
	 */
	@NotNull
	GenericAttributeValue<String> getTargetEntity();


	/**
	 * Returns the value of the fetch child.
	 * @return the value of the fetch child.
	 */
	@NotNull
	GenericAttributeValue<FetchType> getFetch();


	/**
	 * Returns the value of the optional child.
	 * @return the value of the optional child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getOptional();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the mapped-by-id child.
	 * @return the value of the mapped-by-id child.
	 */
	@NotNull
	GenericAttributeValue<String> getMappedById();


	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getId();


	/**
	 * Returns the value of the cascade child.
	 * @return the value of the cascade child.
	 */
	@NotNull
	CascadeType getCascade();


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
	 * Returns the value of the join-table child.
	 * @return the value of the join-table child.
	 */
	@NotNull
	JoinTable getJoinTable();


}
