// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.MappingClassResolveConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:tuplizer interface.
 * Type tuplizer documentation
 * <pre>
 *     TUPLIZER element; defines tuplizer to use for a component/entity for a given entity-mode
 * </pre>
 */
public interface HbmTuplizer extends CommonDomModelElement {

	/**
	 * Returns the value of the entity-mode child.
	 * Attribute entity-mode
	 * @return the value of the entity-mode child.
	 */
	@NotNull
	GenericAttributeValue<String> getEntityMode();


	/**
	 * Returns the value of the class child.
	 * Attribute class
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
        @Convert(MappingClassResolveConverter.class)
        GenericAttributeValue<PsiClass> getClazz();


}
