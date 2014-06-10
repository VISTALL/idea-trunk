// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:entity interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:entity documentation</h3>
 * Defines the settings and mappings for an entity. Is allowed to be
 *         sparsely populated and used in conjunction with the annotations.
 *         Alternatively, the metadata-complete attribute can be used to 
 *         indicate that no annotations on the entity class (and its fields
 *         or properties) are to be processed. If this is the case then 
 *         the defaulting rules for the entity and its subelements will 
 *         be recursively applied.
 *         @Target(TYPE) @Retention(RUNTIME)
 *           public @interface Entity {
 *           String name() default "";
 *         }
 * </pre>
 */
public interface Entity extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClazz();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the cacheable child.
	 * @return the value of the cacheable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getCacheable();


	/**
	 * Returns the value of the metadata-complete child.
	 * @return the value of the metadata-complete child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getMetadataComplete();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the table child.
	 * @return the value of the table child.
	 */
	@NotNull
	Table getTable();


	/**
	 * Returns the list of secondary-table children.
	 * @return the list of secondary-table children.
	 */
	@NotNull
	List<SecondaryTable> getSecondaryTables();
	/**
	 * Adds new child to the list of secondary-table children.
	 * @return created child
	 */
	SecondaryTable addSecondaryTable();


	/**
	 * Returns the list of primary-key-join-column children.
	 * @return the list of primary-key-join-column children.
	 */
	@NotNull
	List<PrimaryKeyJoinColumn> getPrimaryKeyJoinColumns();
	/**
	 * Adds new child to the list of primary-key-join-column children.
	 * @return created child
	 */
	PrimaryKeyJoinColumn addPrimaryKeyJoinColumn();


	/**
	 * Returns the value of the id-class child.
	 * @return the value of the id-class child.
	 */
	@NotNull
	IdClass getIdClass();


	/**
	 * Returns the value of the inheritance child.
	 * @return the value of the inheritance child.
	 */
	@NotNull
	Inheritance getInheritance();


	/**
	 * Returns the value of the discriminator-value child.
	 * @return the value of the discriminator-value child.
	 */
	@NotNull
	GenericDomValue<String> getDiscriminatorValue();


	/**
	 * Returns the value of the discriminator-column child.
	 * @return the value of the discriminator-column child.
	 */
	@NotNull
	DiscriminatorColumn getDiscriminatorColumn();


	/**
	 * Returns the value of the sequence-generator child.
	 * @return the value of the sequence-generator child.
	 */
	@NotNull
	SequenceGenerator getSequenceGenerator();


	/**
	 * Returns the value of the table-generator child.
	 * @return the value of the table-generator child.
	 */
	@NotNull
	TableGenerator getTableGenerator();


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
	 * Returns the value of the exclude-default-listeners child.
	 * @return the value of the exclude-default-listeners child.
	 */
	@NotNull
	@SubTag (value = "exclude-default-listeners", indicator = true)
	GenericDomValue<Boolean> getExcludeDefaultListeners();


	/**
	 * Returns the value of the exclude-superclass-listeners child.
	 * @return the value of the exclude-superclass-listeners child.
	 */
	@NotNull
	@SubTag (value = "exclude-superclass-listeners", indicator = true)
	GenericDomValue<Boolean> getExcludeSuperclassListeners();


	/**
	 * Returns the value of the entity-listeners child.
	 * @return the value of the entity-listeners child.
	 */
	@NotNull
	EntityListeners getEntityListeners();


	/**
	 * Returns the value of the pre-persist child.
	 * @return the value of the pre-persist child.
	 */
	@NotNull
	PrePersist getPrePersist();


	/**
	 * Returns the value of the post-persist child.
	 * @return the value of the post-persist child.
	 */
	@NotNull
	PostPersist getPostPersist();


	/**
	 * Returns the value of the pre-remove child.
	 * @return the value of the pre-remove child.
	 */
	@NotNull
	PreRemove getPreRemove();


	/**
	 * Returns the value of the post-remove child.
	 * @return the value of the post-remove child.
	 */
	@NotNull
	PostRemove getPostRemove();


	/**
	 * Returns the value of the pre-update child.
	 * @return the value of the pre-update child.
	 */
	@NotNull
	PreUpdate getPreUpdate();


	/**
	 * Returns the value of the post-update child.
	 * @return the value of the post-update child.
	 */
	@NotNull
	PostUpdate getPostUpdate();


	/**
	 * Returns the value of the post-load child.
	 * @return the value of the post-load child.
	 */
	@NotNull
	PostLoad getPostLoad();


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
	 * Returns the value of the attributes child.
	 * @return the value of the attributes child.
	 */
	@NotNull
	Attributes getAttributes();


}
