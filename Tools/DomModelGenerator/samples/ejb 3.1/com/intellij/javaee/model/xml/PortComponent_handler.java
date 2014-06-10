// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:port-component_handlerType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:port-component_handlerType documentation</h3>
 * Declares the handler for a port-component. Handlers can access the
 * 	init-param name/value pairs using the HandlerInfo interface.
 * 	Used in: port-component
 * </pre>
 */
public interface PortComponent_handler extends CommonDomModelElement, DescriptionGroup, JndiEnvironmentRefsGroup, ServiceRefGroup {

	/**
	 * Returns the value of the handler-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:handler-name documentation</h3>
	 * Defines the name of the handler. The name must be unique within the
	 * 	    module.
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
	 * Defines a fully qualified class name for the handler implementation.
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
	 * Defines the QName of a SOAP header that will be processed by the
	 * 	    handler.
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
	 * The soap-role element contains a SOAP actor definition that the
	 * 	    Handler will play as a role.
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


	/**
	 * Returns the list of env-entry children.
	 * @return the list of env-entry children.
	 */
	@NotNull
	List<EnvEntry> getEnvEntries();
	/**
	 * Adds new child to the list of env-entry children.
	 * @return created child
	 */
	EnvEntry addEnvEntry();


	/**
	 * Returns the list of ejb-ref children.
	 * @return the list of ejb-ref children.
	 */
	@NotNull
	List<EjbRef> getEjbRefs();
	/**
	 * Adds new child to the list of ejb-ref children.
	 * @return created child
	 */
	EjbRef addEjbRef();


	/**
	 * Returns the list of ejb-local-ref children.
	 * @return the list of ejb-local-ref children.
	 */
	@NotNull
	List<EjbLocalRef> getEjbLocalRefs();
	/**
	 * Adds new child to the list of ejb-local-ref children.
	 * @return created child
	 */
	EjbLocalRef addEjbLocalRef();


	/**
	 * Returns the list of resource-ref children.
	 * @return the list of resource-ref children.
	 */
	@NotNull
	List<ResourceRef> getResourceRefs();
	/**
	 * Adds new child to the list of resource-ref children.
	 * @return created child
	 */
	ResourceRef addResourceRef();


	/**
	 * Returns the list of resource-env-ref children.
	 * @return the list of resource-env-ref children.
	 */
	@NotNull
	List<ResourceEnvRef> getResourceEnvRefs();
	/**
	 * Adds new child to the list of resource-env-ref children.
	 * @return created child
	 */
	ResourceEnvRef addResourceEnvRef();


	/**
	 * Returns the list of message-destination-ref children.
	 * @return the list of message-destination-ref children.
	 */
	@NotNull
	List<MessageDestinationRef> getMessageDestinationRefs();
	/**
	 * Adds new child to the list of message-destination-ref children.
	 * @return created child
	 */
	MessageDestinationRef addMessageDestinationRef();


	/**
	 * Returns the list of persistence-context-ref children.
	 * @return the list of persistence-context-ref children.
	 */
	@NotNull
	List<PersistenceContextRef> getPersistenceContextRefs();
	/**
	 * Adds new child to the list of persistence-context-ref children.
	 * @return created child
	 */
	PersistenceContextRef addPersistenceContextRef();


	/**
	 * Returns the list of persistence-unit-ref children.
	 * @return the list of persistence-unit-ref children.
	 */
	@NotNull
	List<PersistenceUnitRef> getPersistenceUnitRefs();
	/**
	 * Adds new child to the list of persistence-unit-ref children.
	 * @return created child
	 */
	PersistenceUnitRef addPersistenceUnitRef();


	/**
	 * Returns the list of post-construct children.
	 * @return the list of post-construct children.
	 */
	@NotNull
	List<LifecycleCallback> getPostConstructs();
	/**
	 * Adds new child to the list of post-construct children.
	 * @return created child
	 */
	LifecycleCallback addPostConstruct();


	/**
	 * Returns the list of pre-destroy children.
	 * @return the list of pre-destroy children.
	 */
	@NotNull
	List<LifecycleCallback> getPreDestroys();
	/**
	 * Adds new child to the list of pre-destroy children.
	 * @return created child
	 */
	LifecycleCallback addPreDestroy();


	/**
	 * Returns the list of service-ref children.
	 * @return the list of service-ref children.
	 */
	@NotNull
	List<ServiceRef> getServiceRefs();
	/**
	 * Adds new child to the list of service-ref children.
	 * @return created child
	 */
	ServiceRef addServiceRef();


}
