// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelRootElement;
import com.intellij.javaee.model.xml.converters.PackageNameConverter;
import com.intellij.javaee.model.JavaeePersistenceORMResolveConverters;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.hibernate.model.converters.CascadeTypeListConverter;
import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:hibernate-mapping interface.
 * Type hibernate-mapping documentation
 * <pre>
 * 	The document root.
 *  
 * </pre>
 */
public interface HbmHibernateMapping extends CommonDomModelRootElement, PersistenceMappings {

	/**
	 * Returns the value of the package child.
	 * Attribute package
	 * @return the value of the package child.
	 */
	@NotNull
        @Convert(PackageNameConverter.class)
        GenericAttributeValue<PsiPackage> getPackage();


	/**
	 * Returns the value of the catalog child.
	 * Attribute catalog
	 * @return the value of the catalog child.
	 */
	@NotNull
        @Convert(JavaeePersistenceORMResolveConverters.CatalogResolver.class)
        GenericAttributeValue<String> getCatalog();


	/**
	 * Returns the value of the default-cascade child.
	 * Attribute default-cascade
	 * @return the value of the default-cascade child.
	 */
	@NotNull
        @Convert(CascadeTypeListConverter.class)
        GenericAttributeValue<List<CascadeType>> getDefaultCascade();


	/**
	 * Returns the value of the schema child.
	 * Attribute schema
	 * @return the value of the schema child.
	 */
	@NotNull
        @Convert(JavaeePersistenceORMResolveConverters.SchemaResolver.class)
        GenericAttributeValue<String> getSchema();


	/**
	 * Returns the value of the default-access child.
	 * Attribute default-access
	 * @return the value of the default-access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getDefaultAccess();


	/**
	 * Returns the value of the auto-import child.
	 * Attribute auto-import
	 * @return the value of the auto-import child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getAutoImport();


	/**
	 * Returns the value of the default-lazy child.
	 * Attribute default-lazy
	 * @return the value of the default-lazy child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getDefaultLazy();


	/**
	 * Returns the list of meta children.
	 * Type meta documentation
	 * <pre>
	 * 	META element definition; used to assign meta-level attributes to a class
	 * 	or property.  Is currently used by codegenerator as a placeholder for
	 * 	values that is not directly related to OR mappings.
	 * </pre>
	 * @return the list of meta children.
	 */
	@NotNull
	List<HbmMeta> getMetas();
	/**
	 * Adds new child to the list of meta children.
	 * @return created child
	 */
	HbmMeta addMeta();


	/**
	 * Returns the list of typedef children.
	 * Type typedef documentation
	 * <pre>
	 * 	TYPEDEF element definition; defines a new name for a Hibernate type. May
	 * 	contain parameters for parameterizable types.
	 * </pre>
	 * @return the list of typedef children.
	 */
	@NotNull
	List<HbmTypedef> getTypedefs();
	/**
	 * Adds new child to the list of typedef children.
	 * @return created child
	 */
	HbmTypedef addTypedef();


	/**
	 * Returns the list of import children.
	 * Type import documentation
	 * <pre>
	 * 	IMPORT element definition; an explicit query language "import"
	 * </pre>
	 * @return the list of import children.
	 */
	@NotNull
	List<HbmImport> getImports();
	/**
	 * Adds new child to the list of import children.
	 * @return created child
	 */
	HbmImport addImport();


	/**
	 * Returns the list of resultset children.
	 * Type resultset documentation
	 * <pre>
	 *  The resultset element declares a named resultset mapping definition for SQL queries 
	 * </pre>
	 * @return the list of resultset children.
	 */
	@NotNull
	List<HbmResultset> getResultsets();
	/**
	 * Adds new child to the list of resultset children.
	 * @return created child
	 */
	HbmResultset addResultset();


	/**
	 * Returns the list of filter-def children.
	 * Type filter-def documentation
	 * <pre>
	 * 	FILTER-DEF element; top-level filter definition.
	 * </pre>
	 * @return the list of filter-def children.
	 */
	@NotNull
	List<HbmFilterDef> getFilterDefs();
	/**
	 * Adds new child to the list of filter-def children.
	 * @return created child
	 */
	HbmFilterDef addFilterDef();


	/**
	 * Returns the list of database-object children.
	 * Type database-object documentation
	 * <pre>
	 *     Element for defining "auxiliary" database objects.  Must be one of two forms:
	 *     #1 :
	 *         <database-object>
	 *             <definition class="CustomClassExtendingAuxiliaryObject"/>
	 *         </database-object>
	 *     #2 :
	 *         <database-object>
	 *             <create>CREATE OR REPLACE ....</create>
	 *             <drop>DROP ....</drop>
	 *         </database-object>
	 * </pre>
	 * @return the list of database-object children.
	 */
	@NotNull
	List<HbmDatabaseObject> getDatabaseObjects();
	/**
	 * Adds new child to the list of database-object children.
	 * @return created child
	 */
	HbmDatabaseObject addDatabaseObject();


	/**
	 * Returns the list of class children.
	 * Type class documentation
	 * <pre>
	 * 	Root entity mapping.  Poorly named as entities do not have to be represented by 
	 * 	classes at all.  Mapped entities may be represented via different methodologies 
	 * 	(POJO, Map, Dom4j).
	 * </pre>
	 * @return the list of class children.
	 */
	@NotNull
	List<HbmClass> getClasses();
	/**
	 * Adds new child to the list of class children.
	 * @return created child
	 */
	HbmClass addClass();


	/**
	 * Returns the list of subclass children.
	 * Type subclass documentation
	 * <pre>
	 * 	Subclass declarations are nested beneath the root class declaration to achieve
	 * 	polymorphic persistence with the table-per-hierarchy mapping strategy.
	 * 	See the note on the class element regarding <pojo/> vs. @name usage...
	 * </pre>
	 * @return the list of subclass children.
	 */
	@NotNull
	List<HbmSubclass> getSubclasses();
	/**
	 * Adds new child to the list of subclass children.
	 * @return created child
	 */
	HbmSubclass addSubclass();


	/**
	 * Returns the list of joined-subclass children.
	 * Type joined-subclass documentation
	 * <pre>
	 * 	Joined subclasses are used for the normalized table-per-subclass mapping strategy
	 * 	See the note on the class element regarding <pojo/> vs. @name usage...
	 * </pre>
	 * @return the list of joined-subclass children.
	 */
	@NotNull
	List<HbmJoinedSubclass> getJoinedSubclasses();
	/**
	 * Adds new child to the list of joined-subclass children.
	 * @return created child
	 */
	HbmJoinedSubclass addJoinedSubclass();


	/**
	 * Returns the list of union-subclass children.
	 * Type union-subclass documentation
	 * <pre>
	 * 	Union subclasses are used for the table-per-concrete-class mapping strategy
	 * 	See the note on the class element regarding <pojo/> vs. @name usage...
	 * </pre>
	 * @return the list of union-subclass children.
	 */
	@NotNull
	List<HbmUnionSubclass> getUnionSubclasses();
	/**
	 * Adds new child to the list of union-subclass children.
	 * @return created child
	 */
	HbmUnionSubclass addUnionSubclass();


	/**
	 * Returns the list of query children.
	 * Type query documentation
	 * <pre>
	 *  The query element declares a named Hibernate query string 
	 * </pre>
	 * @return the list of query children.
	 */
	@NotNull
	List<HbmQuery> getQueries();
	/**
	 * Adds new child to the list of query children.
	 * @return created child
	 */
	HbmQuery addQuery();


	/**
	 * Returns the list of sql-query children.
	 * Type sql-query documentation
	 * <pre>
	 *  The sql-query element declares a named SQL query string 
	 * </pre>
	 * @return the list of sql-query children.
	 */
	@NotNull
	List<HbmSqlQuery> getSqlQueries();
	/**
	 * Adds new child to the list of sql-query children.
	 * @return created child
	 */
	HbmSqlQuery addSqlQuery();


}
