// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:jndiEnvironmentRefsGroup model group interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:jndiEnvironmentRefsGroup documentation</h3>
 * This group keeps the usage of the contained JNDI environment
 * 	reference elements consistent across Java EE deployment descriptors.
 * </pre>
 */
public interface JndiEnvironmentRefsGroup extends ServiceRefGroup {

	@NotNull
	List<EnvEntry> getEnvEntries();
	EnvEntry addEnvEntry();


	@NotNull
	List<EjbRef> getEjbRefs();
	EjbRef addEjbRef();


	@NotNull
	List<EjbLocalRef> getEjbLocalRefs();
	EjbLocalRef addEjbLocalRef();


	@NotNull
	List<ResourceRef> getResourceRefs();
	ResourceRef addResourceRef();


	@NotNull
	List<ResourceEnvRef> getResourceEnvRefs();
	ResourceEnvRef addResourceEnvRef();


	@NotNull
	List<MessageDestinationRef> getMessageDestinationRefs();
	MessageDestinationRef addMessageDestinationRef();


	@NotNull
	List<PersistenceContextRef> getPersistenceContextRefs();
	PersistenceContextRef addPersistenceContextRef();


	@NotNull
	List<PersistenceUnitRef> getPersistenceUnitRefs();
	PersistenceUnitRef addPersistenceUnitRef();


	@NotNull
	List<LifecycleCallback> getPostConstructs();
	LifecycleCallback addPostConstruct();


	@NotNull
	List<LifecycleCallback> getPreDestroys();
	LifecycleCallback addPreDestroy();


	@NotNull
	List<ServiceRef> getServiceRefs();
	ServiceRef addServiceRef();


}
