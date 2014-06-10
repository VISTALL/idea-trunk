// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:attributes interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:attributes documentation</h3>
 * This element contains the entity field or property mappings.
 *         It may be sparsely populated to include only a subset of the
 *         fields or properties. If metadata-complete for the entity is true
 *         then the remainder of the attributes will be defaulted according
 *         to the default rules.
 * </pre>
 */
public interface Attributes extends JavaeeDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the list of basic children.
	 * @return the list of basic children.
	 */
	@NotNull
	List<Basic> getBasics();
	/**
	 * Adds new child to the list of basic children.
	 * @return created child
	 */
	Basic addBasic();


	/**
	 * Returns the list of version children.
	 * @return the list of version children.
	 */
	@NotNull
	List<Version> getVersions();
	/**
	 * Adds new child to the list of version children.
	 * @return created child
	 */
	Version addVersion();


	/**
	 * Returns the list of many-to-one children.
	 * @return the list of many-to-one children.
	 */
	@NotNull
	List<ManyToOne> getManyToOnes();
	/**
	 * Adds new child to the list of many-to-one children.
	 * @return created child
	 */
	ManyToOne addManyToOne();


	/**
	 * Returns the list of one-to-many children.
	 * @return the list of one-to-many children.
	 */
	@NotNull
	List<OneToMany> getOneToManies();
	/**
	 * Adds new child to the list of one-to-many children.
	 * @return created child
	 */
	OneToMany addOneToMany();


	/**
	 * Returns the list of one-to-one children.
	 * @return the list of one-to-one children.
	 */
	@NotNull
	List<OneToOne> getOneToOnes();
	/**
	 * Adds new child to the list of one-to-one children.
	 * @return created child
	 */
	OneToOne addOneToOne();


	/**
	 * Returns the list of many-to-many children.
	 * @return the list of many-to-many children.
	 */
	@NotNull
	List<ManyToMany> getManyToManies();
	/**
	 * Adds new child to the list of many-to-many children.
	 * @return created child
	 */
	ManyToMany addManyToMany();


	/**
	 * Returns the list of element-collection children.
	 * @return the list of element-collection children.
	 */
	@NotNull
	List<ElementCollection> getElementCollections();
	/**
	 * Adds new child to the list of element-collection children.
	 * @return created child
	 */
	ElementCollection addElementCollection();


	/**
	 * Returns the list of embedded children.
	 * @return the list of embedded children.
	 */
	@NotNull
	List<Embedded> getEmbeddeds();
	/**
	 * Adds new child to the list of embedded children.
	 * @return created child
	 */
	Embedded addEmbedded();


	/**
	 * Returns the list of transient children.
	 * @return the list of transient children.
	 */
	@NotNull
	List<Transient> getTransients();
	/**
	 * Adds new child to the list of transient children.
	 * @return created child
	 */
	Transient addTransient();


	/**
	 * Returns the list of id children.
	 * @return the list of id children.
	 */
	@NotNull
	List<Id> getIds();
	/**
	 * Adds new child to the list of id children.
	 * @return created child
	 */
	Id addId();


	/**
	 * Returns the value of the embedded-id child.
	 * @return the value of the embedded-id child.
	 */
	@NotNull
	EmbeddedId getEmbeddedId();


}
