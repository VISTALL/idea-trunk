// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:security-role-refType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:security-role-refType documentation</h3>
 * The security-role-refType contains the declaration of a
 * 	security role reference in a component's or a
 * 	Deployment Component's code. The declaration consists of an
 * 	optional description, the security role name used in the
 * 	code, and an optional link to a security role. If the
 * 	security role is not specified, the Deployer must choose an
 * 	appropriate security role.
 * </pre>
 */
public interface SecurityRoleRef extends CommonDomModelElement {

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
	 * <h3>Element http://java.sun.com/xml/ns/javaee:role-name documentation</h3>
	 * The value of the role-name element must be the String used
	 * 	    as the parameter to the
	 * 	    EJBContext.isCallerInRole(String roleName) method or the
	 * 	    HttpServletRequest.isUserInRole(String role) method.
	 * </pre>
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


	/**
	 * Returns the value of the role-link child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:role-link documentation</h3>
	 * The role-link element is a reference to a defined
	 * 	    security role. The role-link element must contain
	 * 	    the name of one of the security roles defined in the
	 * 	    security-role elements.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:role-nameType documentation</h3>
	 * The role-nameType designates the name of a security role.
	 * 	The name must conform to the lexical rules for a token.
	 * </pre>
	 * @return the value of the role-link child.
	 */
	@NotNull
	GenericDomValue<String> getRoleLink();


}
