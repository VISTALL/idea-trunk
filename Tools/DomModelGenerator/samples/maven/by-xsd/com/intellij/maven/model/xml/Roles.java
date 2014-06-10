// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:rolesElemType interface.
 */
public interface Roles extends DomElement {

	/**
	 * Returns the list of role children.
	 * @return the list of role children.
	 */
	@NotNull
	List<GenericDomValue<String>> getRoles();
	/**
	 * Adds new child to the list of role children.
	 * @return created child
	 */
	GenericDomValue<String> addRole();


}
