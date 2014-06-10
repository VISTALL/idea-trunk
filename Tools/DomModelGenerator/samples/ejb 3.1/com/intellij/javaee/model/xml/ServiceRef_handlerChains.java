// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:service-ref_handler-chainsType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:service-ref_handler-chainsType documentation</h3>
 * The handler-chains element defines the handlerchains associated with this
 *       service or service endpoint.
 * </pre>
 */
public interface ServiceRef_handlerChains extends CommonDomModelElement {

	/**
	 * Returns the list of handler-chain children.
	 * @return the list of handler-chain children.
	 */
	@NotNull
	List<ServiceRef_handlerChain> getHandlerChains();
	/**
	 * Adds new child to the list of handler-chain children.
	 * @return created child
	 */
	ServiceRef_handlerChain addHandlerChain();


}
