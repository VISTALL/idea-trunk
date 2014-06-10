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
 * http://java.sun.com/xml/ns/persistence/orm:entity-mappingsElemType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:entity-mappingsElemType documentation</h3>
 * The entity-mappings element is the root element of an mapping
 *         file. It contains the following four types of elements:
 *         1. The persistence-unit-metadata element contains metadata
 *         for the entire persistence unit. It is undefined if this element
 *         occurs in multiple mapping files within the same persistence unit.
 *         
 *         2. The package, schema, catalog and access elements apply to all of
 *         the entity, mapped-superclass and embeddable elements defined in
 *         the same file in which they occur.
 *         3. The sequence-generator, table-generator, named-query,
 *         named-native-query and sql-result-set-mapping elements are global
 *         to the persistence unit. It is undefined to have more than one
 *         sequence-generator or table-generator of the same name in the same
 *         or different mapping files in a persistence unit. It is also 
 *         undefined to have more than one named-query, named-native-query, or
 *         result-set-mapping of the same name in the same or different mapping 
 * files in a persistence unit.
 *         4. The entity, mapped-superclass and embeddable elements each define
 *         the mapping information for a managed persistent class. The mapping
 *         information contained in these elements may be complete or it may
 *         be partial.
 * </pre>
 */
public interface EntityMappings extends JavaeeDomModelElement {

	/**
	 * Returns the value of the version child.
	 * @return the value of the version child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getVersion();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the persistence-unit-metadata child.
	 * @return the value of the persistence-unit-metadata child.
	 */
	@NotNull
	PersistenceUnitMetadata getPersistenceUnitMetadata();


	/**
	 * Returns the value of the package child.
	 * @return the value of the package child.
	 */
	@NotNull
	GenericDomValue<String> getPackage();


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
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericDomValue<AccessType> getAccess();


	/**
	 * Returns the list of sequence-generator children.
	 * @return the list of sequence-generator children.
	 */
	@NotNull
	List<SequenceGenerator> getSequenceGenerators();
	/**
	 * Adds new child to the list of sequence-generator children.
	 * @return created child
	 */
	SequenceGenerator addSequenceGenerator();


	/**
	 * Returns the list of table-generator children.
	 * @return the list of table-generator children.
	 */
	@NotNull
	List<TableGenerator> getTableGenerators();
	/**
	 * Adds new child to the list of table-generator children.
	 * @return created child
	 */
	TableGenerator addTableGenerator();


	/**
	 * Returns the list of named-query children.
	 * @return the list of named-query children.
	 */
	@NotNull
	List<NamedQuery> getNamedQueries();
	/**
	 * Adds new child to the list of named-query children.
	 * @return created child
	 */
	NamedQuery addNamedQuery();


	/**
	 * Returns the list of named-native-query children.
	 * @return the list of named-native-query children.
	 */
	@NotNull
	List<NamedNativeQuery> getNamedNativeQueries();
	/**
	 * Adds new child to the list of named-native-query children.
	 * @return created child
	 */
	NamedNativeQuery addNamedNativeQuery();


	/**
	 * Returns the list of sql-result-set-mapping children.
	 * @return the list of sql-result-set-mapping children.
	 */
	@NotNull
	List<SqlResultSetMapping> getSqlResultSetMappings();
	/**
	 * Adds new child to the list of sql-result-set-mapping children.
	 * @return created child
	 */
	SqlResultSetMapping addSqlResultSetMapping();


	/**
	 * Returns the list of mapped-superclass children.
	 * @return the list of mapped-superclass children.
	 */
	@NotNull
	List<MappedSuperclass> getMappedSuperclasses();
	/**
	 * Adds new child to the list of mapped-superclass children.
	 * @return created child
	 */
	MappedSuperclass addMappedSuperclass();


	/**
	 * Returns the list of entity children.
	 * @return the list of entity children.
	 */
	@NotNull
	List<Entity> getEntities();
	/**
	 * Adds new child to the list of entity children.
	 * @return created child
	 */
	Entity addEntity();


	/**
	 * Returns the list of embeddable children.
	 * @return the list of embeddable children.
	 */
	@NotNull
	List<Embeddable> getEmbeddables();
	/**
	 * Adds new child to the list of embeddable children.
	 * @return created child
	 */
	Embeddable addEmbeddable();


}
