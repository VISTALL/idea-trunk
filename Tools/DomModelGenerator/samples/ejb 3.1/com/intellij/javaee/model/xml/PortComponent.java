// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:port-componentType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:port-componentType documentation</h3>
 * The port-component element associates a WSDL port with a web service
 * 	interface and implementation.  It defines the name of the port as a
 * 	component, optional description, optional display name, optional iconic
 * 	representations, WSDL port QName, Service Endpoint Interface, Service
 * 	Implementation Bean.
 * 	This element also associates a WSDL service with a JAX-WS Provider
 * 	implementation.
 * </pre>
 */
public interface PortComponent extends CommonDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	Description getDescription();


	/**
	 * Returns the value of the display-name child.
	 * @return the value of the display-name child.
	 */
	@NotNull
	DisplayName getDisplayName();


	/**
	 * Returns the value of the icon child.
	 * @return the value of the icon child.
	 */
	@NotNull
	Icon getIcon();


	/**
	 * Returns the value of the port-component-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:port-component-name documentation</h3>
	 * 	      The port-component-name element specifies a port component's
	 * 	      name.  This name is assigned by the module producer to name
	 * 	      the service implementation bean in the module's deployment
	 * 	      descriptor. The name must be unique among the port component
	 * 	      names defined in the same module.
	 * 	      Used in: port-component
	 * 	      Example:
	 * 		      <port-component-name>EmployeeService
	 * 		      </port-component-name>
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the port-component-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getPortComponentName();


	/**
	 * Returns the value of the wsdl-service child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:wsdl-service documentation</h3>
	 * Defines the name space and local name part of the WSDL
	 * 	    service QName. This is required to be specified for
	 * 	    port components that are JAX-WS Provider implementations.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdQNameType documentation</h3>
	 * This type adds an "id" attribute to xsd:QName.
	 * </pre>
	 * @return the value of the wsdl-service child.
	 */
	@NotNull
	GenericDomValue<String> getWsdlService();


	/**
	 * Returns the value of the wsdl-port child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:wsdl-port documentation</h3>
	 * Defines the name space and local name part of the WSDL
	 * 	    port QName. This is not required to be specified for port
	 * 	    components that are JAX-WS Provider implementations
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdQNameType documentation</h3>
	 * This type adds an "id" attribute to xsd:QName.
	 * </pre>
	 * @return the value of the wsdl-port child.
	 */
	@NotNull
	GenericDomValue<String> getWsdlPort();


	/**
	 * Returns the value of the enable-mtom child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:enable-mtom documentation</h3>
	 * Used to enable or disable SOAP MTOM/XOP mechanism for an
	 *             endpoint implementation. By default its value is false.
	 *             If this element is not specified, default value is assumed.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the enable-mtom child.
	 */
	@NotNull
	GenericDomValue<Boolean> getEnableMtom();


	/**
	 * Returns the value of the protocol-binding child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:protocol-binding documentation</h3>
	 * Used to specify the protocol binding used by the port-component.
	 * 	    If this element is not specified, then the default binding is
	 *             used (SOAP 1.1 over HTTP)
	 * </pre>
	 * @return the value of the protocol-binding child.
	 */
	@NotNull
	GenericDomValue<String> getProtocolBinding();


	/**
	 * Returns the value of the service-endpoint-interface child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:service-endpoint-interface documentation</h3>
	 * 	      The service-endpoint-interface element contains the
	 * 	      fully-qualified name of the port component's Service Endpoint
	 * 	      Interface.
	 * 	      Used in: port-component
	 * 	      Example:
	 * 		      <remote>com.wombat.empl.EmployeeService</remote>
	 * 	      This may not be specified in case there is no Service
	 * 	      Enpoint Interface as is the case with directly using an
	 * 	      implementation class with the @WebService annotation.
	 * 	      When the port component is a Provider implementation
	 * 	      this is not specified.
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the service-endpoint-interface child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getServiceEndpointInterface();


	/**
	 * Returns the value of the service-impl-bean child.
	 * @return the value of the service-impl-bean child.
	 */
	@NotNull
	@Required
	ServiceImplBean getServiceImplBean();


	/**
	 * Returns the list of handler children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:handler documentation</h3>
	 * To be used with JAX-RPC based runtime only.
	 * </pre>
	 * @return the list of handler children.
	 */
	@NotNull
	List<PortComponent_handler> getHandlers();
	/**
	 * Adds new child to the list of handler children.
	 * @return created child
	 */
	PortComponent_handler addHandler();


	/**
	 * Returns the value of the handler-chains child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:handler-chains documentation</h3>
	 * To be used with JAX-WS based runtime only.
	 * </pre>
	 * @return the value of the handler-chains child.
	 */
	@NotNull
	HandlerChains getHandlerChains();


}
