// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:handler-chainType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:handler-chainType documentation</h3>
 * The handler-chain element defines the handlerchain.
 *       Handlerchain can be defined such that the handlers in the
 *       handlerchain operate,all ports of a service, on a specific
 *       port or on a list of protocol-bindings. The choice of elements
 *       service-name-pattern, port-name-pattern and protocol-bindings
 *       are used to specify whether the handlers in handler-chain are
 *       for a service, port or protocol binding. If none of these
 *       choices are specified with the handler-chain element then the
 *       handlers specified in the handler-chain will be applied on
 *       everything.
 * </pre>
 */
public interface HandlerChain extends CommonDomModelElement {

	/**
	 * Returns the list of handler children.
	 * @return the list of handler children.
	 */
	@NotNull
	@Required
	List<PortComponent_handler> getHandlers();
	/**
	 * Adds new child to the list of handler children.
	 * @return created child
	 */
	PortComponent_handler addHandler();


	/**
	 * Returns the value of the service-name-pattern child.
	 * @return the value of the service-name-pattern child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getServiceNamePattern();


	/**
	 * Returns the value of the port-name-pattern child.
	 * @return the value of the port-name-pattern child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getPortNamePattern();


	/**
	 * Returns the value of the protocol-bindings child.
	 * @return the value of the protocol-bindings child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getProtocolBindings();


}
