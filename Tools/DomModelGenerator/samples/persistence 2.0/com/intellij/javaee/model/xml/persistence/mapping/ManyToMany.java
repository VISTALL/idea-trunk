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
 * http://java.sun.com/xml/ns/persistence/orm:many-to-many interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:many-to-many documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface ManyToMany {
 *           Class targetEntity() default void.class;
 *           CascadeType[] cascade() default {};
 *           FetchType fetch() default LAZY;
 *           String mappedBy() default "";
 *         }
 * </pre>
 */
public interface ManyToMany extends JavaeeDomModelElement {

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
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the mapped-by child.
	 * @return the value of the mapped-by child.
	 */
	@NotNull
	GenericAttributeValue<String> getMappedBy();


	/**
	 * Returns the value of the join-table child.
	 * @return the value of the join-table child.
	 */
	@NotNull
	JoinTable getJoinTable();


	/**
	 * Returns the value of the cascade child.
	 * @return the value of the cascade child.
	 */
	@NotNull
	CascadeType getCascade();


	/**
	 * Returns the value of the order-by child.
	 * @return the value of the order-by child.
	 */
	@NotNull
	GenericDomValue<String> getOrderBy();


	/**
	 * Returns the value of the order-column child.
	 * @return the value of the order-column child.
	 */
	@NotNull
	OrderColumn getOrderColumn();


	/**
	 * Returns the value of the map-key child.
	 * @return the value of the map-key child.
	 */
	@NotNull
	MapKey getMapKey();


	/**
	 * Returns the value of the map-key-class child.
	 * @return the value of the map-key-class child.
	 */
	@NotNull
	MapKeyClass getMapKeyClass();


	/**
	 * Returns the value of the map-key-temporal child.
	 * @return the value of the map-key-temporal child.
	 */
	@NotNull
	GenericDomValue<Temporal> getMapKeyTemporal();


	/**
	 * Returns the value of the map-key-enumerated child.
	 * @return the value of the map-key-enumerated child.
	 */
	@NotNull
	GenericDomValue<Enumerated> getMapKeyEnumerated();


	/**
	 * Returns the value of the map-key-attribute-override child.
	 * @return the value of the map-key-attribute-override child.
	 */
	@NotNull
	AttributeOverride getMapKeyAttributeOverride();


	/**
	 * Returns the value of the map-key-column child.
	 * @return the value of the map-key-column child.
	 */
	@NotNull
	MapKeyColumn getMapKeyColumn();


	/**
	 * Returns the list of map-key-join-column children.
	 * @return the list of map-key-join-column children.
	 */
	@NotNull
	List<MapKeyJoinColumn> getMapKeyJoinColumns();
	/**
	 * Adds new child to the list of map-key-join-column children.
	 * @return created child
	 */
	MapKeyJoinColumn addMapKeyJoinColumn();


}
