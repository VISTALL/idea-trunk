// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiType;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Namespace;
import com.intellij.spring.constants.SpringConstants;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:valueElemType interface.
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringValue extends GenericDomValue<Object>, TypeHolder {

	/**
	 * Returns the value of the type child.
	 * <pre>
	 * <h3>Attribute null:type documentation</h3>
	 * 	The exact type that the value should be converted to. Only needed
	 * 	if the type of the target property or constructor argument is
	 * 	too generic: for example, in case of a collection element.
	 * 					
	 * </pre>
	 * @return the value of the type child.
	 */
	@NotNull
	GenericAttributeValue<PsiType> getType();
}
