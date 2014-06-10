// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.PropertyTypeResolvingConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.JavaeePersistenceORMResolveConverters;
import com.intellij.psi.PsiType;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:discriminator interface.
 * Type discriminator documentation
 * <pre>
 *  Polymorphic data requires a column holding a class discriminator value. This
 * value is not directly exposed to the application. 
 * </pre>
 */
public interface HbmDiscriminator extends CommonDomModelElement {

	/**
	 * Returns the value of the not-null child.
	 * Attribute not-null
	 * @return the value of the not-null child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getNotNull();


	/**
	 * Returns the value of the type child.
	 * Attribute type
	 * @return the value of the type child.
	 */
	@NotNull
        @Convert(PropertyTypeResolvingConverter.class)
        GenericAttributeValue<PsiType> getType();


	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("column")
        @Convert(JavaeePersistenceORMResolveConverters.ColumnResolver.class)
        GenericAttributeValue<String> getColumnAttr();


	/**
	 * Returns the value of the length child.
	 * Attribute length
	 * @return the value of the length child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getLength();


	/**
	 * Returns the value of the insert child.
	 * Attribute insert
	 * @return the value of the insert child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getInsert();


	/**
	 * Returns the value of the force child.
	 * Attribute force
	 * @return the value of the force child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getForce();


	/**
	 * Returns the value of the formula child.
	 * Attribute formula
	 * @return the value of the formula child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("formula")
	GenericAttributeValue<String> getFormulaAttr();


	/**
	 * Returns the value of the column child.
	 * Type column documentation
	 * <pre>
	 *  The column element is an alternative to column attributes and required for 
	 * mapping associations to classes with composite ids. 
	 * </pre>
	 * @return the value of the column child.
	 */
	@NotNull
	HbmColumn getColumn();


	/**
	 * Returns the value of the formula child.
	 * Type formula documentation
	 * <pre>
	 *  The formula and subselect elements allow us to map derived properties and 
	 * entities. 
	 * </pre>
	 * @return the value of the formula child.
	 */
	@NotNull
	GenericDomValue<String> getFormula();


}
