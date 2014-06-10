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
 * http://java.sun.com/xml/ns/persistence/orm:element-collection interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:element-collection documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface ElementCollection {
 *           Class targetClass() default void.class;
 *           FetchType fetch() default LAZY;
 *         }
 * </pre>
 */
public interface ElementCollection extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the target-class child.
	 * @return the value of the target-class child.
	 */
	@NotNull
	GenericAttributeValue<String> getTargetClass();


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
	 * Returns the value of the collection-table child.
	 * @return the value of the collection-table child.
	 */
	@NotNull
	CollectionTable getCollectionTable();


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
	 * Returns the value of the column child.
	 * @return the value of the column child.
	 */
	@NotNull
	Column getColumn();


	/**
	 * Returns the list of attribute-override children.
	 * @return the list of attribute-override children.
	 */
	@NotNull
	List<AttributeOverride> getAttributeOverrides();
	/**
	 * Adds new child to the list of attribute-override children.
	 * @return created child
	 */
	AttributeOverride addAttributeOverride();


	/**
	 * Returns the list of association-override children.
	 * @return the list of association-override children.
	 */
	@NotNull
	List<AssociationOverride> getAssociationOverrides();
	/**
	 * Adds new child to the list of association-override children.
	 * @return created child
	 */
	AssociationOverride addAssociationOverride();


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


	/**
	 * Returns the value of the temporal child.
	 * @return the value of the temporal child.
	 */
	@NotNull
	GenericDomValue<Temporal> getTemporal();


	/**
	 * Returns the value of the enumerated child.
	 * @return the value of the enumerated child.
	 */
	@NotNull
	GenericDomValue<Enumerated> getEnumerated();


	/**
	 * Returns the value of the lob child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:lob documentation</h3>
	 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
	 *         public @interface Lob {}
	 * </pre>
	 * @return the value of the lob child.
	 */
	@NotNull
	GenericDomValue<String> getLob();


}
