// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://jboss.org/xml/ns/javax/validation/mapping:constraint-mappingsType interface.
 */
public interface ConstraintMappings extends DomElement {

	/**
	 * Returns the value of the default-package child.
	 * @return the value of the default-package child.
	 */
	@NotNull
	GenericDomValue<String> getDefaultPackage();


	/**
	 * Returns the list of bean children.
	 * @return the list of bean children.
	 */
	@NotNull
	List<Bean> getBeans();
	/**
	 * Adds new child to the list of bean children.
	 * @return created child
	 */
	Bean addBean();


	/**
	 * Returns the list of constraint-definition children.
	 * @return the list of constraint-definition children.
	 */
	@NotNull
	List<ConstraintDefinition> getConstraintDefinitions();
	/**
	 * Adds new child to the list of constraint-definition children.
	 * @return created child
	 */
	ConstraintDefinition addConstraintDefinition();


}
