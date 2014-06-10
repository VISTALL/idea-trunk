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
 * hibernate-mapping-3.0.dtd:import interface.
 * Type import documentation
 * <pre>
 * 	IMPORT element definition; an explicit query language "import"
 * </pre>
 */
public interface HbmImport extends CommonDomModelElement {

	/**
	 * Returns the value of the rename child.
	 * Attribute rename
	 * @return the value of the rename child.
	 */
	@NotNull
	GenericAttributeValue<String> getRename();


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
