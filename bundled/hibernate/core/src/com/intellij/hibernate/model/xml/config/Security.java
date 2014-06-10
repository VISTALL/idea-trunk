// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-configuration-3.0.dtd:security interface.
 * Type security documentation
 * <pre>
 *  the JNDI name 
 * </pre>
 */
public interface Security extends CommonDomModelElement {

	/**
	 * Returns the value of the context child.
	 * Attribute context
	 * @return the value of the context child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getContext();


	/**
	 * Returns the list of grant children.
	 * Type grant documentation
	 * <pre>
	 * the JACC contextID
	 * </pre>
	 * @return the list of grant children.
	 */
	@NotNull
	List<Grant> getGrants();
	/**
	 * Adds new child to the list of grant children.
	 * @return created child
	 */
	Grant addGrant();


}
