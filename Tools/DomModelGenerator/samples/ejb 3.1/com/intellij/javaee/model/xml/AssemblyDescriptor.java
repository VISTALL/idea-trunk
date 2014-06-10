// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.ApplicationException;
import com.intellij.javaee.model.xml.ejb.ContainerTransaction;
import com.intellij.javaee.model.xml.ejb.ExcludeList;
import com.intellij.javaee.model.xml.ejb.InterceptorBinding;
import com.intellij.javaee.model.xml.ejb.MethodPermission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:assembly-descriptorType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:assembly-descriptorType documentation</h3>
 * The assembly-descriptorType defines
 * 	application-assembly information.
 * 	The application-assembly information consists of the
 * 	following parts: the definition of security roles, the
 * 	definition of method permissions, the definition of
 * 	transaction attributes for enterprise beans with
 * 	container-managed transaction demarcation, the definition
 *         of interceptor bindings, a list of
 * 	methods to be excluded from being invoked, and a list of
 *         exception types that should be treated as application exceptions.
 * 	All the parts are optional in the sense that they are
 * 	omitted if the lists represented by them are empty.
 * 	Providing an assembly-descriptor in the deployment
 * 	descriptor is optional for the ejb-jar file producer.
 * </pre>
 */
public interface AssemblyDescriptor extends CommonDomModelElement {

	/**
	 * Returns the list of security-role children.
	 * @return the list of security-role children.
	 */
	@NotNull
	List<SecurityRole> getSecurityRoles();
	/**
	 * Adds new child to the list of security-role children.
	 * @return created child
	 */
	SecurityRole addSecurityRole();


	/**
	 * Returns the list of method-permission children.
	 * @return the list of method-permission children.
	 */
	@NotNull
	List<MethodPermission> getMethodPermissions();
	/**
	 * Adds new child to the list of method-permission children.
	 * @return created child
	 */
	MethodPermission addMethodPermission();


	/**
	 * Returns the list of container-transaction children.
	 * @return the list of container-transaction children.
	 */
	@NotNull
	List<ContainerTransaction> getContainerTransactions();
	/**
	 * Adds new child to the list of container-transaction children.
	 * @return created child
	 */
	ContainerTransaction addContainerTransaction();


	/**
	 * Returns the list of interceptor-binding children.
	 * @return the list of interceptor-binding children.
	 */
	@NotNull
	List<InterceptorBinding> getInterceptorBindings();
	/**
	 * Adds new child to the list of interceptor-binding children.
	 * @return created child
	 */
	InterceptorBinding addInterceptorBinding();


	/**
	 * Returns the list of message-destination children.
	 * @return the list of message-destination children.
	 */
	@NotNull
	List<MessageDestination> getMessageDestinations();
	/**
	 * Adds new child to the list of message-destination children.
	 * @return created child
	 */
	MessageDestination addMessageDestination();


	/**
	 * Returns the value of the exclude-list child.
	 * @return the value of the exclude-list child.
	 */
	@NotNull
	ExcludeList getExcludeList();


	/**
	 * Returns the list of application-exception children.
	 * @return the list of application-exception children.
	 */
	@NotNull
	List<ApplicationException> getApplicationExceptions();
	/**
	 * Adds new child to the list of application-exception children.
	 * @return created child
	 */
	ApplicationException addApplicationException();


}
