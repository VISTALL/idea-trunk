// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-configuration-3.0.dtd:grant interface.
 * Type grant documentation
 * <pre>
 * the JACC contextID
 * </pre>
 */
public interface Grant extends CommonDomModelElement {

	/**
	 * Returns the value of the entity-name child.
	 * Attribute entity-name
	 * @return the value of the entity-name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getEntityName();


	/**
	 * Returns the value of the actions child.
	 * Attribute actions
	 * @return the value of the actions child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getActions();


	/**
	 * Returns the value of the role child.
	 * Attribute role
	 * @return the value of the role child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getRole();


}
