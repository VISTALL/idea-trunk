// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.TransAttribute;
import com.intellij.javaee.model.xml.ejb.Method;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:container-transactionType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:container-transactionType documentation</h3>
 * The container-transactionType specifies how the container
 * 	must manage transaction scopes for the enterprise bean's
 * 	method invocations. It defines an optional description, a
 * 	list of method elements, and a transaction attribute. The
 * 	transaction attribute is to be applied to all the specified
 * 	methods.
 * </pre>
 */
public interface ContainerTransaction extends CommonDomModelElement {

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
	 * Returns the value of the trans-attribute child.
	 * @return the value of the trans-attribute child.
	 */
	@NotNull
	@Required
	GenericDomValue<TransAttribute> getTransAttribute();


}
