// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:typedCollectionType interface.
 */
public interface TypedCollection extends DomElement, BaseCollection {

	/**
	 * Returns the value of the value-type child.
	 * <pre>
	 * <h3>Attribute null:value-type documentation</h3>
	 * 	The default Java type for nested values. Must be a fully qualified
	 * 	class name.
	 * 						
	 * </pre>
	 * @return the value of the value-type child.
	 */
	@NotNull
	GenericAttributeValue<PsiClass> getValueType();

}
