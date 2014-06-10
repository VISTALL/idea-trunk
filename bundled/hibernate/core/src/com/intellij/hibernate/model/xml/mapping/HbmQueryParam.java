// Generated on Fri Mar 30 16:27:20 MSD 2007
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.NameValue;
import com.intellij.hibernate.model.converters.PropertyTypeResolvingConverter;
import com.intellij.psi.PsiType;
import com.intellij.persistence.model.PersistenceQueryParam;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:query-param interface.
 * Type query-param documentation
 * <pre>
 *  The query-param element is used only by tools that generate
 * finder methods for named queries
 * </pre>
 */
public interface HbmQueryParam extends CommonDomModelElement, PersistenceQueryParam {

	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
        @NameValue
        GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the type child.
	 * Attribute type
	 * @return the value of the type child.
	 */
	@NotNull
	@Required
        @Convert(PropertyTypeResolvingConverter.class)
        GenericAttributeValue<PsiType> getType();


}
