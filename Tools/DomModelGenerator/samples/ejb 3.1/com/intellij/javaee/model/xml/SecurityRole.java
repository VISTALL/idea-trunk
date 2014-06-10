// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:security-roleType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:security-roleType documentation</h3>
 * 	  The security-roleType contains the definition of a security
 * 	  role. The definition consists of an optional description of
 * 	  the security role, and the security role name.
 * 	  Example:
 * 	      <security-role>
 * 	      <description>
 * 		  This role includes all employees who are authorized
 * 		  to access the employee service application.
 * 	      </description>
 * 	      <role-name>employee</role-name>
 * 	      </security-role>
 * 	  
 * </pre>
 */
public interface SecurityRole extends CommonDomModelElement {

	/**
	 * Returns the list of description children.
	 * @return the list of description children.
	 */
	@NotNull
	List<Description> getDescriptions();
	/**
	 * Adds new child to the list of description children.
	 * @return created child
	 */
	Description addDescription();


	/**
	 * Returns the value of the role-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:role-nameType documentation</h3>
	 * The role-nameType designates the name of a security role.
	 * 	The name must conform to the lexical rules for a token.
	 * </pre>
	 * @return the value of the role-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getRoleName();


}
