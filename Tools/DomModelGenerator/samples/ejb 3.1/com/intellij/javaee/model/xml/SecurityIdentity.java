// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:security-identityType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:security-identityType documentation</h3>
 * The security-identityType specifies whether the caller's
 * 	security identity is to be used for the execution of the
 * 	methods of the enterprise bean or whether a specific run-as
 * 	identity is to be used. It contains an optional description
 * 	and a specification of the security identity to be used.
 * </pre>
 */
public interface SecurityIdentity extends CommonDomModelElement {

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
	 * Returns the value of the use-caller-identity child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:use-caller-identity documentation</h3>
	 * The use-caller-identity element specifies that
	 * 	      the caller's security identity be used as the
	 * 	      security identity for the execution of the
	 * 	      enterprise bean's methods.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:emptyType documentation</h3>
	 * This type is used to designate an empty
	 * 	element when used.
	 * </pre>
	 * @return the value of the use-caller-identity child.
	 */
	@NotNull
	@SubTag (value = "use-caller-identity", indicator = true)
	@Required
	GenericDomValue<Boolean> getUseCallerIdentity();


	/**
	 * Returns the value of the run-as child.
	 * @return the value of the run-as child.
	 */
	@NotNull
	@Required
	RunAs getRunAs();


}
