// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.AroundInvoke;
import com.intellij.javaee.model.xml.ejb.AroundTimeout;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:interceptorType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:interceptorType documentation</h3>
 * The interceptorType element declares information about a single
 *         interceptor class.  It consists of :
 *             - An optional description.
 *             - The fully-qualified name of the interceptor class.
 *             - An optional list of around invoke methods declared on the
 *               interceptor class and/or its super-classes.
 *             - An optional list of around timeout methods declared on the
 *               interceptor class and/or its super-classes.
 *             - An optional list environment dependencies for the interceptor
 *               class and/or its super-classes.
 *             - An optional list of post-activate methods declared on the
 *               interceptor class and/or its super-classes.
 *             - An optional list of pre-passivate methods declared on the
 *               interceptor class and/or its super-classes.
 * </pre>
 */
public interface Interceptor extends CommonDomModelElement, JndiEnvironmentRefsGroup, ServiceRefGroup {

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
	 * Returns the value of the interceptor-class child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the interceptor-class child.
	 */
	@NotNull
	@Required
	GenericDomValue<PsiClass> getInterceptorClass();


	/**
	 * Returns the list of around-invoke children.
	 * @return the list of around-invoke children.
	 */
	@NotNull
	List<AroundInvoke> getAroundInvokes();
	/**
	 * Adds new child to the list of around-invoke children.
	 * @return created child
	 */
	AroundInvoke addAroundInvoke();


	/**
	 * Returns the list of around-timeout children.
	 * @return the list of around-timeout children.
	 */
	@NotNull
	List<AroundTimeout> getAroundTimeouts();
	/**
	 * Adds new child to the list of around-timeout children.
	 * @return created child
	 */
	AroundTimeout addAroundTimeout();


	/**
	 * Returns the list of post-activate children.
	 * @return the list of post-activate children.
	 */
	@NotNull
	List<LifecycleCallback> getPostActivates();
	/**
	 * Adds new child to the list of post-activate children.
	 * @return created child
	 */
	LifecycleCallback addPostActivate();


	/**
	 * Returns the list of pre-passivate children.
	 * @return the list of pre-passivate children.
	 */
	@NotNull
	List<LifecycleCallback> getPrePassivates();
	/**
	 * Adds new child to the list of pre-passivate children.
	 * @return created child
	 */
	LifecycleCallback addPrePassivate();


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
