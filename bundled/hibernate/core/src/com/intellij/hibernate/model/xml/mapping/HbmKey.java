// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.enums.OnDeleteType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:key interface.
 * Type key documentation
 * <pre>
 *  Declares the column name of a foreign key. 
 * </pre>
 */
public interface HbmKey extends CommonDomModelElement, HbmColumnsHolderBase {

	/**
	 * Returns the value of the not-null child.
	 * Attribute not-null
	 * @return the value of the not-null child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getNotNull();


	/**
	 * Returns the value of the foreign-key child.
	 * Attribute foreign-key
	 * @return the value of the foreign-key child.
	 */
	@NotNull
	GenericAttributeValue<String> getForeignKey();


	/**
	 * Returns the value of the property-ref child.
	 * Attribute property-ref
	 * @return the value of the property-ref child.
	 */
	@NotNull
	GenericAttributeValue<String> getPropertyRef();


	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
        GenericAttributeValue<String> getColumn();


	/**
	 * Returns the value of the on-delete child.
	 * Attribute on-delete
	 * @return the value of the on-delete child.
	 */
	@NotNull
	GenericAttributeValue<OnDeleteType> getOnDelete();


	/**
	 * Returns the value of the unique child.
	 * Attribute unique
	 * @return the value of the unique child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUnique();


	/**
	 * Returns the value of the update child.
	 * Attribute update
	 * @return the value of the update child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUpdate();


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


}
