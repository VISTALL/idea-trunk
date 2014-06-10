// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.CascadeTypeListConverter;
import com.intellij.hibernate.model.converters.MappingClassResolveConverter;
import com.intellij.hibernate.model.converters.LazyTypeConverter;
import com.intellij.hibernate.model.enums.*;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:set interface.
 */
public interface HbmSet extends CommonDomModelElement, HbmContainer {

	/**
	 * Returns the value of the persister child.
	 * Attribute persister
	 * @return the value of the persister child.
	 */
	@NotNull
        @Convert(MappingClassResolveConverter.class)
        GenericAttributeValue<PsiClass> getPersister();


	/**
	 * Returns the value of the inverse child.
	 * Attribute inverse
	 * @return the value of the inverse child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getInverse();


	/**
	 * Returns the value of the collection-type child.
	 * Attribute collection-type
	 * @return the value of the collection-type child.
	 */
	@NotNull
	GenericAttributeValue<String> getCollectionType();


	/**
	 * Returns the value of the cascade child.
	 * Attribute cascade
	 * @return the value of the cascade child.
	 */
	@NotNull
        @Convert(CascadeTypeListConverter.class)
        GenericAttributeValue<List<CascadeType>> getCascade();


	/**
	 * Returns the value of the access child.
	 * Attribute access
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the order-by child.
	 * Attribute order-by
	 * @return the value of the order-by child.
	 */
	@NotNull
	GenericAttributeValue<String> getOrderBy();


	/**
	 * Returns the value of the catalog child.
	 * Attribute catalog
	 * @return the value of the catalog child.
	 */
	@NotNull
	GenericAttributeValue<String> getCatalog();


	/**
	 * Returns the value of the optimistic-lock child.
	 * Attribute optimistic-lock
	 * @return the value of the optimistic-lock child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getOptimisticLock();


	/**
	 * Returns the value of the where child.
	 * Attribute where
	 * @return the value of the where child.
	 */
	@NotNull
	GenericAttributeValue<String> getWhere();


	/**
	 * Returns the value of the outer-join child.
	 * Attribute outer-join
	 * @return the value of the outer-join child.
	 */
	@NotNull
	GenericAttributeValue<String> getOuterJoin();


	/**
	 * Returns the value of the subselect child.
	 * Attribute subselect
	 * @return the value of the subselect child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("subselect")
	GenericAttributeValue<String> getSubselectAttr();


	/**
	 * Returns the value of the embed-xml child.
	 * Attribute embed-xml
	 * @return the value of the embed-xml child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getEmbedXml();


	/**
	 * Returns the value of the schema child.
	 * Attribute schema
	 * @return the value of the schema child.
	 */
	@NotNull
	GenericAttributeValue<String> getSchema();


	/**
	 * Returns the value of the fetch child.
	 * Attribute fetch
	 * @return the value of the fetch child.
	 */
	@NotNull
	GenericAttributeValue<CollectionFetchType> getFetch();


	/**
	 * Returns the value of the node child.
	 * Attribute node
	 * @return the value of the node child.
	 */
	@NotNull
	GenericAttributeValue<String> getNode();


	/**
	 * Returns the value of the mutable child.
	 * Attribute mutable
	 * @return the value of the mutable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getMutable();


	/**
	 * Returns the value of the lazy child.
	 * Attribute lazy
	 * @return the value of the lazy child.
	 */
	@NotNull
        @Convert(LazyTypeConverter.class)
        GenericAttributeValue<LazyType> getLazy();


	/**
	 * Returns the value of the table child.
	 * Attribute table
	 * @return the value of the table child.
	 */
	@NotNull
        @Attribute("table")
        GenericAttributeValue<String> getTableName();


	/**
	 * Returns the value of the check child.
	 * Attribute check
	 * @return the value of the check child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getCheck();


	/**
	 * Returns the value of the sort child.
	 * Attribute sort
	 * @return the value of the sort child.
	 */
	@NotNull
	GenericAttributeValue<String> getSort();


	/**
	 * Returns the value of the batch-size child.
	 * Attribute batch-size
	 * @return the value of the batch-size child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getBatchSize();


	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	//@NotNull
	//@Required
	//GenericAttributeValue<String> getName();


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
	 * Returns the value of the subselect child.
	 * @return the value of the subselect child.
	 */
	@NotNull
	GenericDomValue<String> getSubselect();


	/**
	 * Returns the value of the cache child.
	 * Type cache documentation
	 * <pre>
	 *  The cache element enables caching of an entity class. 
	 * </pre>
	 * @return the value of the cache child.
	 */
	@NotNull
	HbmCache getCache();


	/**
	 * Returns the list of synchronize children.
	 * @return the list of synchronize children.
	 */
	@NotNull
	List<HbmSynchronize> getSynchronizes();
	/**
	 * Adds new child to the list of synchronize children.
	 * @return created child
	 */
	HbmSynchronize addSynchronize();


	/**
	 * Returns the value of the comment child.
	 * Type comment documentation
	 * <pre>
	 *  The comment element allows definition of a database table or column comment. 
	 * </pre>
	 * @return the value of the comment child.
	 */
	@NotNull
	GenericDomValue<String> getComment();


	/**
	 * Returns the value of the key child.
	 * Type key documentation
	 * <pre>
	 *  Declares the column name of a foreign key. 
	 * </pre>
	 * @return the value of the key child.
	 */
	@NotNull
	@Required
	HbmKey getKey();


	/**
	 * Returns the value of the loader child.
	 * Type loader documentation
	 * <pre>
	 *  The loader element allows specification of a named query to be used for fetching
	 * an entity or collection 
	 * </pre>
	 * @return the value of the loader child.
	 */
	@NotNull
	HbmLoader getLoader();


	/**
	 * Returns the value of the sql-insert child.
	 * Type sql-insert documentation
	 * <pre>
	 *  custom sql operations 
	 * </pre>
	 * @return the value of the sql-insert child.
	 */
	@NotNull
	HbmSqlStatement getSqlInsert();


	/**
	 * Returns the value of the sql-update child.
	 * @return the value of the sql-update child.
	 */
	@NotNull
	HbmSqlStatement getSqlUpdate();


	/**
	 * Returns the value of the sql-delete child.
	 * @return the value of the sql-delete child.
	 */
	@NotNull
	HbmSqlStatement getSqlDelete();


	/**
	 * Returns the value of the sql-delete-all child.
	 * @return the value of the sql-delete-all child.
	 */
	@NotNull
	HbmSqlStatement getSqlDeleteAll();


	/**
	 * Returns the list of filter children.
	 * Type filter documentation
	 * <pre>
	 * 	FILTER element; used to apply a filter.
	 * </pre>
	 * @return the list of filter children.
	 */
	@NotNull
	List<HbmFilter> getFilters();
	/**
	 * Adds new child to the list of filter children.
	 * @return created child
	 */
	HbmFilter addFilter();


	/**
	 * Returns the value of the element child.
	 * Type element documentation
	 * <pre>
	 *  Declares the element type of a collection of basic type 
	 * </pre>
	 * @return the value of the element child.
	 */
	@NotNull
	HbmElement getElement();


	/**
	 * Returns the value of the one-to-many child.
	 * Type one-to-many documentation
	 * <pre>
	 *  One to many association. This tag declares the entity-class
	 * element type of a collection and specifies a one-to-many relational model 
	 * </pre>
	 * @return the value of the one-to-many child.
	 */
	@NotNull
	HbmOneToMany getOneToMany();


	/**
	 * Returns the value of the many-to-many child.
	 * Type many-to-many documentation
	 * <pre>
	 *  Many to many association. This tag declares the entity-class
	 * element type of a collection and specifies a many-to-many relational model 
	 * </pre>
	 * @return the value of the many-to-many child.
	 */
	@NotNull
	HbmManyToMany getManyToMany();


	/**
	 * Returns the value of the composite-element child.
	 * Type composite-element documentation
	 * <pre>
	 *  A composite element allows a collection to hold instances of an arbitrary 
	 * class, without the requirement of joining to an entity table. Composite elements
	 * have component semantics - no shared references and ad hoc null value semantics. 
	 * Composite elements may not hold nested collections. 
	 * </pre>
	 * @return the value of the composite-element child.
	 */
	@NotNull
	HbmCompositeElement getCompositeElement();


	/**
	 * Returns the value of the many-to-any child.
	 * Type many-to-any documentation
	 * <pre>
	 *  A "many to any" defines a polymorphic association to any table 
	 * with the given identifier type. The first listed column is a VARCHAR column 
	 * holding the name of the class (for that row). 
	 * </pre>
	 * @return the value of the many-to-any child.
	 */
	@NotNull
	HbmManyToAny getManyToAny();


}
