// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:service-ref_handlerType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:service-ref_handlerType documentation</h3>
 * Declares the handler for a port-component. Handlers can access the
 * 	init-param name/value pairs using the HandlerInfo interface. If
 * 	port-name is not specified, the handler is assumed to be associated
 * 	with all ports of the service.
 * 	Used in: service-ref
 * </pre>
 */
public interface ServiceRef_handler extends CommonDomModelElement, DescriptionGroup {

	/**
	 * Returns the value of the handler-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:handler-name documentation</h3>
	 * Defines the name of the handler. The name must be unique
	 * 	    within the module.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the handler-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getHandlerName();


	/**
	 * Returns the value of the handler-class child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:handler-class documentation</h3>
	 * Defines a fully qualified class name for the handler
	 * 	    implementation.
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
	 * @return the value of the handler-class child.
	 */
	@NotNull
	@Required
	GenericDomValue<PsiClass> getHandlerClass();


	/**
	 * Returns the list of init-param children.
	 * @return the list of init-param children.
	 */
	@NotNull
	List<ParamValue> getInitParams();
	/**
	 * Adds new child to the list of init-param children.
	 * @return created child
	 */
	ParamValue addInitParam();


	/**
	 * Returns the list of soap-header children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:soap-header documentation</h3>
	 * Defines the QName of a SOAP header that will be processed
	 * 	    by the handler.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdQNameType documentation</h3>
	 * This type adds an "id" attribute to xsd:QName.
	 * </pre>
	 * @return the list of soap-header children.
	 */
	@NotNull
	List<GenericDomValue<String>> getSoapHeaders();
	/**
	 * Adds new child to the list of soap-header children.
	 * @return created child
	 */
	GenericDomValue<String> addSoapHeader();


	/**
	 * Returns the list of soap-role children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:soap-role documentation</h3>
	 * The soap-role element contains a SOAP actor definition that
	 * 	    the Handler will play as a role.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the list of soap-role children.
	 */
	@NotNull
	List<GenericDomValue<String>> getSoapRoles();
	/**
	 * Adds new child to the list of soap-role children.
	 * @return created child
	 */
	GenericDomValue<String> addSoapRole();


	/**
	 * Returns the list of port-name children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:port-name documentation</h3>
	 * The port-name element defines the WSDL port-name that a
	 * 	    handler should be associated with.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the list of port-name children.
	 */
	@NotNull
	List<GenericDomValue<String>> getPortNames();
	/**
	 * Adds new child to the list of port-name children.
	 * @return created child
	 */
	GenericDomValue<String> addPortName();


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
	 * Returns the list of display-name children.
	 * @return the list of display-name children.
	 */
	@NotNull
	List<DisplayName> getDisplayNames();
	/**
	 * Adds new child to the list of display-name children.
	 * @return created child
	 */
	DisplayName addDisplayName();


	/**
	 * Returns the list of icon children.
	 * @return the list of icon children.
	 */
	@NotNull
	List<Icon> getIcons();
	/**
	 * Adds new child to the list of icon children.
	 * @return created child
	 */
	Icon addIcon();


}
