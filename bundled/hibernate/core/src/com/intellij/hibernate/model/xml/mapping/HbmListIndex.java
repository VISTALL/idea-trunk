// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.JavaeePersistenceORMResolveConverters;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:list-index interface.
 * Type list-index documentation
 * <pre>
 *  Declares the type and column mapping for a collection index (array or
 * list index, or key of a map). 
 * </pre>
 */
public interface HbmListIndex extends CommonDomModelElement {

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
	 * Returns the value of the base child.
	 * Attribute base
	 * @return the value of the base child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getBase();


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


}
