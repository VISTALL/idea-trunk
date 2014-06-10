// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.LazyTypeConverter;
import com.intellij.hibernate.model.enums.FetchType;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.enums.NotFoundType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.xml.converters.AttributeConverter;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.PersistentEntity;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:many-to-many interface.
 * Type many-to-many documentation
 * <pre>
 *  Many to many association. This tag declares the entity-class
 * element type of a collection and specifies a many-to-many relational model 
 * </pre>
 */
public interface HbmManyToMany extends CommonDomModelElement, HbmRelationAttributeBase.AnyToManyBase, HbmRelationAttributeBase.NonOneToManyBase, HbmColumnsHolderBase {

	/**
	 * Returns the value of the fetch child.
	 * Attribute fetch
	 * @return the value of the fetch child.
	 */
	@NotNull
	GenericAttributeValue<FetchType> getFetch();


	/**
	 * Returns the value of the lazy child.
	 * Attribute lazy
	 * @return the value of the lazy child.
	 */
	@NotNull
	@Convert(LazyTypeConverter.class)
        GenericAttributeValue<LazyType> getLazy();



	/**
	 * Returns the value of the foreign-key child.
	 * Attribute foreign-key
	 * @return the value of the foreign-key child.
	 */
	@NotNull
	GenericAttributeValue<String> getForeignKey();


	/**
	 * Returns the value of the node child.
	 * Attribute node
	 * @return the value of the node child.
	 */
	@NotNull
	GenericAttributeValue<String> getNode();


	/**
	 * Returns the value of the entity-name child.
	 * Attribute entity-name
	 * @return the value of the entity-name child.
	 */
	@NotNull
        @SubTag("entity-name")
        GenericAttributeValue<PersistentEntity> getEntityNameValue();

  /**
	 * Returns the value of the class child.
	 * Attribute class
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	GenericAttributeValue<PsiClass> getClazz();


	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
        GenericAttributeValue<String> getColumn();


	/**
	 * Returns the value of the embed-xml child.
	 * Attribute embed-xml
	 * @return the value of the embed-xml child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getEmbedXml();


	/**
	 * Returns the value of the outer-join child.
	 * Attribute outer-join
	 * @return the value of the outer-join child.
	 */
	@NotNull
	GenericAttributeValue<String> getOuterJoin();


	/**
	 * Returns the value of the unique child.
	 * Attribute unique
	 * @return the value of the unique child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUnique();


	/**
	 * Returns the value of the where child.
	 * Attribute where
	 * @return the value of the where child.
	 */
	@NotNull
	GenericAttributeValue<String> getWhere();


	/**
	 * Returns the value of the not-found child.
	 * Attribute not-found
	 * @return the value of the not-found child.
	 */
	@NotNull
	GenericAttributeValue<NotFoundType> getNotFound();


	/**
	 * Returns the value of the order-by child.
	 * Attribute order-by
	 * @return the value of the order-by child.
	 */
	@NotNull
	GenericAttributeValue<String> getOrderBy();


	/**
	 * Returns the value of the property-ref child.
	 * Attribute property-ref
	 * @return the value of the property-ref child.
	 */
	@NotNull
        @Convert(AttributeConverter.class)
        GenericAttributeValue<PersistentAttribute> getPropertyRef();


	/**
	 * Returns the value of the formula child.
	 * Attribute formula
	 * @return the value of the formula child.
	 */
	@NotNull
	GenericAttributeValue<String> getFormula();


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
	 * Returns the list of column children.
	 * Type column documentation
	 * <pre>
	 *  The column element is an alternative to column attributes and required for 
	 * mapping associations to classes with composite ids. 
	 * </pre>
	 * @return the list of column children.
	 */
	@NotNull
	List<HbmColumn> getColumns();
	/**
	 * Adds new child to the list of column children.
	 * @return created child
	 */
	HbmColumn addColumn();


	/**
	 * Returns the list of formula children.
	 * Type formula documentation
	 * <pre>
	 *  The formula and subselect elements allow us to map derived properties and 
	 * entities. 
	 * </pre>
	 * @return the list of formula children.
	 */
	@NotNull
	List<GenericDomValue<String>> getFormulas();
	/**
	 * Adds new child to the list of formula children.
	 * @return created child
	 */
	GenericDomValue<String> addFormula();


}
