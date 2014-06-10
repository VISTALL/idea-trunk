// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.Method;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:method-permissionType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:method-permissionType documentation</h3>
 * The method-permissionType specifies that one or more
 * 	security roles are allowed to invoke one or more enterprise
 * 	bean methods. The method-permissionType consists of an
 * 	optional description, a list of security role names or an
 * 	indicator to state that the method is unchecked for
 * 	authorization, and a list of method elements.
 * 	The security roles used in the method-permissionType
 * 	must be defined in the security-role elements of the
 * 	deployment descriptor, and the methods must be methods
 * 	defined in the enterprise bean's business, home, component
 *         and/or web service endpoint interfaces.
 * </pre>
 */
public interface MethodPermission extends CommonDomModelElement {

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
	 * Returns the list of method children.
	 * @return the list of method children.
	 */
	@NotNull
	@Required
	List<Method> getMethods();
	/**
	 * Adds new child to the list of method children.
	 * @return created child
	 */
	Method addMethod();


	/**
	 * Returns the list of role-name children.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:role-nameType documentation</h3>
	 * The role-nameType designates the name of a security role.
	 * 	The name must conform to the lexical rules for a token.
	 * </pre>
	 * @return the list of role-name children.
	 */
	@NotNull
	@Required
	List<GenericDomValue<String>> getRoleNames();
	/**
	 * Adds new child to the list of role-name children.
	 * @return created child
	 */
	GenericDomValue<String> addRoleName();


	/**
	 * Returns the value of the unchecked child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:unchecked documentation</h3>
	 * The unchecked element specifies that a method is
	 * 	      not checked for authorization by the container
	 * 	      prior to invocation of the method.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:emptyType documentation</h3>
	 * This type is used to designate an empty
	 * 	element when used.
	 * </pre>
	 * @return the value of the unchecked child.
	 */
	@NotNull
	@SubTag (value = "unchecked", indicator = true)
	@Required
	GenericDomValue<Boolean> getUnchecked();


}
