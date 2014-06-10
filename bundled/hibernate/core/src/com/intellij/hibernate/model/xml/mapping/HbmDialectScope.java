// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Convert;
import com.intellij.hibernate.model.converters.MappingClassResolveConverter;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:dialect-scope interface.
 * Type dialect-scope documentation
 * <pre>
 *     dialect-scope element allows scoping auxiliary-objects to a particular
 *     Hibernate dialect implementation.
 * </pre>
 */
public interface HbmDialectScope extends CommonDomModelElement {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	String getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(String value);


	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
        @Convert(MappingClassResolveConverter.class)
        GenericAttributeValue<PsiClass> getName();


}
