// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:persistence-unit-defaults interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:persistence-unit-defaults documentation</h3>
 * These defaults are applied to the persistence unit as a whole 
 *         unless they are overridden by local annotation or XML 
 *         element settings. 
 *         
 *         schema - Used as the schema for all tables, secondary tables,
 *             collection tables, sequence generators, and table generators
 * that apply to the persistence unit
 *         catalog - Used as the catalog for all tables, secondary tables, 
 *             collection tables, sequence generators, and table generators
 * that apply to the persistence unit
 *  delimited-identifiers - Used to treat database identifiers as
 * delimited identifiers.
 *         access - Used as the access type for all managed classes in
 *             the persistence unit
 *         cascade-persist - Adds cascade-persist to the set of cascade options
 *             in all entity relationships of the persistence unit
 *         entity-listeners - List of default entity listeners to be invoked 
 *             on each entity in the persistence unit.
 * </pre>
 */
public interface PersistenceUnitDefaults extends JavaeeDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the schema child.
	 * @return the value of the schema child.
	 */
	@NotNull
	GenericDomValue<String> getSchema();


	/**
	 * Returns the value of the catalog child.
	 * @return the value of the catalog child.
	 */
	@NotNull
	GenericDomValue<String> getCatalog();


	/**
	 * Returns the value of the delimited-identifiers child.
	 * @return the value of the delimited-identifiers child.
	 */
	@NotNull
	@SubTag (value = "delimited-identifiers", indicator = true)
	GenericDomValue<Boolean> getDelimitedIdentifiers();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericDomValue<AccessType> getAccess();


	/**
	 * Returns the value of the cascade-persist child.
	 * @return the value of the cascade-persist child.
	 */
	@NotNull
	@SubTag (value = "cascade-persist", indicator = true)
	GenericDomValue<Boolean> getCascadePersist();


	/**
	 * Returns the value of the entity-listeners child.
	 * @return the value of the entity-listeners child.
	 */
	@NotNull
	EntityListeners getEntityListeners();


}
