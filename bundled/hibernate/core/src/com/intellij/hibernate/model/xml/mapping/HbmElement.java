// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.common.mapping.CollectionOfElements;
import com.intellij.hibernate.model.converters.PropertyTypeResolvingConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.psi.PsiType;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:element interface.
 * Type element documentation
 * <pre>
 *  Declares the element type of a collection of basic type 
 * </pre>
 */
public interface HbmElement extends CommonDomModelElement, HbmCollectionAttributeBase, HbmColumnsHolderBase, HbmTypeHolderBase,
                                    CollectionOfElements {

	/**
	 * Returns the value of the length child.
	 * Attribute length
	 * @return the value of the length child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getLength();


	/**
	 * Returns the value of the node child.
	 * Attribute node
	 * @return the value of the node child.
	 */
	@NotNull
	GenericAttributeValue<String> getNode();


	/**
	 * Returns the value of the scale child.
	 * Attribute scale
	 * @return the value of the scale child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getScale();


	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
        GenericAttributeValue<String> getColumn();


	/**
	 * Returns the value of the unique child.
	 * Attribute unique
	 * @return the value of the unique child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUnique();


	/**
	 * Returns the value of the precision child.
	 * Attribute precision
	 * @return the value of the precision child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getPrecision();


	/**
	 * Returns the value of the not-null child.
	 * Attribute not-null
	 * @return the value of the not-null child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getNotNull();


	/**
	 * Returns the value of the formula child.
	 * Attribute formula
	 * @return the value of the formula child.
	 */
	@NotNull
	GenericAttributeValue<String> getFormula();


	/**
	 * Returns the value of the type child.
	 * Attribute type
	 * @return the value of the type child.
	 */
	@NotNull
        @Convert(PropertyTypeResolvingConverter.class)
        @com.intellij.util.xml.Attribute ("type")
	GenericAttributeValue<PsiType> getTypeAttr();


	/**
	 * Returns the value of the type child.
	 * Type type documentation
	 * <pre>
	 *  Declares the type of the containing property (overrides an eventually existing type
	 * attribute of the property). May contain param elements to customize a ParametrizableType. 
	 * </pre>
	 * @return the value of the type child.
	 */
	@NotNull
	HbmType getType();


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
