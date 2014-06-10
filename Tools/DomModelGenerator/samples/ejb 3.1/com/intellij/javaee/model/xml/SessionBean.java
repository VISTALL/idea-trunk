// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.ConcurrencyManagementType;
import com.intellij.javaee.model.enums.SessionType;
import com.intellij.javaee.model.enums.TransactionType;
import com.intellij.javaee.model.xml.ejb.AroundInvoke;
import com.intellij.javaee.model.xml.ejb.AroundTimeout;
import com.intellij.javaee.model.xml.ejb.AsyncMethod;
import com.intellij.javaee.model.xml.ejb.ConcurrentMethod;
import com.intellij.javaee.model.xml.ejb.DependsOn;
import com.intellij.javaee.model.xml.ejb.InitMethod;
import com.intellij.javaee.model.xml.ejb.NamedMethod;
import com.intellij.javaee.model.xml.ejb.RemoveMethod;
import com.intellij.javaee.model.xml.ejb.SecurityIdentity;
import com.intellij.javaee.model.xml.ejb.StatefulTimeout;
import com.intellij.javaee.model.xml.ejb.Timer;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:session-beanType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:session-beanType documentation</h3>
 * The session-beanType declares an session bean. The
 * 	declaration consists of:
 * 	    - an optional description
 * 	    - an optional display name
 * 	    - an optional icon element that contains a small and a large
 * 	      icon file name
 * 	    - a name assigned to the enterprise bean
 * 	      in the deployment description
 *             - an optional mapped-name element that can be used to provide
 *               vendor-specific deployment information such as the physical
 *               jndi-name of the session bean's remote home/business interface.
 *               This element is not required to be supported by all
 *               implementations. Any use of this element is non-portable.
 *             - the names of all the remote or local business interfaces,
 *               if any
 * 	    - the names of the session bean's remote home and
 * 	      remote interfaces, if any
 * 	    - the names of the session bean's local home and
 * 	      local interfaces, if any
 * 	    - an optional declaration that this bean exposes a
 * 	      no-interface view
 * 	    - the name of the session bean's web service endpoint
 * 	      interface, if any
 * 	    - the session bean's implementation class
 * 	    - the session bean's state management type
 * 	    - an optional declaration of a stateful session bean's timeout value
 *             - an optional declaration of the session bean's timeout method for
 * 	      handling programmatically created timers
 * 	    - an optional declaration of timers to be automatically created at
 * 	      deployment time
 * 	    - an optional declaration that a Singleton bean has eager
 * 	      initialization
 * 	    - an optional declaration of a Singleton/Stateful bean's concurrency
 * 	      management type
 * 	    - an optional declaration of the method locking metadata
 * 	      for a Singleton with container managed concurrency
 * 	    - an optional declaration of the other Singleton beans in the
 * 	      application that must be initialized before this bean
 * 	    - an optional declaration of the session bean's asynchronous
 * 	      methods
 * 	    - the optional session bean's transaction management type.
 *               If it is not present, it is defaulted to Container.
 * 	    - an optional declaration of a stateful session bean's
 * 	      afterBegin, beforeCompletion, and/or afterCompletion methods
 *             - an optional list of the session bean class and/or
 *               superclass around-invoke methods.
 *             - an optional list of the session bean class and/or
 *               superclass around-timeout methods.
 * 	    - an optional declaration of the bean's
 * 	      environment entries
 * 	    - an optional declaration of the bean's EJB references
 * 	    - an optional declaration of the bean's local
 * 	      EJB references
 * 	    - an optional declaration of the bean's web
 * 	      service references
 * 	    - an optional declaration of the security role
 * 	      references
 * 	    - an optional declaration of the security identity
 * 	      to be used for the execution of the bean's methods
 * 	    - an optional declaration of the bean's resource
 * 	      manager connection factory references
 * 	    - an optional declaration of the bean's resource
 * 	      environment references.
 * 	    - an optional declaration of the bean's message
 * 	      destination references
 * 	The elements that are optional are "optional" in the sense
 * 	that they are omitted when if lists represented by them are
 * 	empty.
 * 	The service-endpoint element may only be specified if the
 * 	bean is a stateless session bean.
 * </pre>
 */
public interface SessionBean extends CommonDomModelElement, DescriptionGroup, JndiEnvironmentRefsGroup, ServiceRefGroup {

	/**
	 * Returns the value of the ejb-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:ejb-nameType documentation</h3>
	 * 	  The ejb-nameType specifies an enterprise bean's name. It is
	 * 	  used by ejb-name elements. This name is assigned by the
	 * 	  ejb-jar file producer to name the enterprise bean in the
	 * 	  ejb-jar file's deployment descriptor. The name must be
	 * 	  unique among the names of the enterprise beans in the same
	 * 	  ejb-jar file.
	 * 	  There is no architected relationship between the used
	 * 	  ejb-name in the deployment descriptor and the JNDI name that
	 * 	  the Deployer will assign to the enterprise bean's home.
	 * 	  The name for an entity bean must conform to the lexical
	 * 	  rules for an NMTOKEN.
	 * 	  Example:
	 * 	  <ejb-name>EmployeeService</ejb-name>
	 * 	  
	 * </pre>
	 * @return the value of the ejb-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getEjbName();


	/**
	 * Returns the value of the mapped-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the mapped-name child.
	 */
	@NotNull
	GenericDomValue<String> getMappedName();


	/**
	 * Returns the value of the home child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:homeType documentation</h3>
	 * 	  The homeType defines the fully-qualified name of
	 * 	  an enterprise bean's home interface.
	 * 	  Example:
	 * 	      <home>com.aardvark.payroll.PayrollHome</home>
	 * 	  
	 * </pre>
	 * @return the value of the home child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getHome();


	/**
	 * Returns the value of the remote child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:remoteType documentation</h3>
	 * 	  The remote element contains the fully-qualified name
	 * 	  of the enterprise bean's remote interface.
	 * 	  Example:
	 * 	      <remote>com.wombat.empl.EmployeeService</remote>
	 * 	  
	 * </pre>
	 * @return the value of the remote child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getRemote();


	/**
	 * Returns the value of the local-home child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:local-homeType documentation</h3>
	 * The local-homeType defines the fully-qualified
	 * 	name of an enterprise bean's local home interface.
	 * </pre>
	 * @return the value of the local-home child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getLocalHome();


	/**
	 * Returns the value of the local child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:localType documentation</h3>
	 * The localType defines the fully-qualified name of an
	 * 	enterprise bean's local interface.
	 * </pre>
	 * @return the value of the local child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getLocal();


	/**
	 * Returns the list of business-local children.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the list of business-local children.
	 */
	@NotNull
	List<GenericDomValue<PsiClass>> getBusinessLocals();
	/**
	 * Adds new child to the list of business-local children.
	 * @return created child
	 */
	GenericDomValue<PsiClass> addBusinessLocal();


	/**
	 * Returns the list of business-remote children.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the list of business-remote children.
	 */
	@NotNull
	List<GenericDomValue<PsiClass>> getBusinessRemotes();
	/**
	 * Adds new child to the list of business-remote children.
	 * @return created child
	 */
	GenericDomValue<PsiClass> addBusinessRemote();


	/**
	 * Returns the value of the local-bean child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:local-bean documentation</h3>
	 * The local-bean element declares that this
	 * 	    session bean exposes a no-interface Local client view.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:emptyType documentation</h3>
	 * This type is used to designate an empty
	 * 	element when used.
	 * </pre>
	 * @return the value of the local-bean child.
	 */
	@NotNull
	@SubTag (value = "local-bean", indicator = true)
	GenericDomValue<Boolean> getLocalBean();


	/**
	 * Returns the value of the service-endpoint child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:service-endpoint documentation</h3>
	 * The service-endpoint element contains the
	 * 	    fully-qualified name of the enterprise bean's web
	 * 	    service endpoint interface. The service-endpoint
	 * 	    element may only be specified for a stateless
	 * 	    session bean. The specified interface must be a
	 * 	    valid JAX-RPC service endpoint interface.
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
	 * @return the value of the service-endpoint child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getServiceEndpoint();


	/**
	 * Returns the value of the ejb-class child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:ejb-class documentation</h3>
	 * The ejb-class element specifies the fully qualified name
	 *              of the bean class for this ejb.  It is required unless
	 *              there is a component-defining annotation for the same
	 *              ejb-name.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:ejb-classType documentation</h3>
	 * 	  The ejb-classType contains the fully-qualified name of the
	 * 	  enterprise bean's class. It is used by ejb-class elements.
	 * 	  Example:
	 * 	      <ejb-class>com.wombat.empl.EmployeeServiceBean</ejb-class>
	 * 	  
	 * </pre>
	 * @return the value of the ejb-class child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getEjbClass();


	/**
	 * Returns the value of the session-type child.
	 * @return the value of the session-type child.
	 */
	@NotNull
	GenericDomValue<SessionType> getSessionType();


	/**
	 * Returns the value of the stateful-timeout child.
	 * @return the value of the stateful-timeout child.
	 */
	@NotNull
	StatefulTimeout getStatefulTimeout();


	/**
	 * Returns the value of the timeout-method child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:timeout-method documentation</h3>
	 * The timeout-method element specifies the method that
	 * 	    will receive callbacks for programmatically
	 * 	    created timers.
	 * </pre>
	 * @return the value of the timeout-method child.
	 */
	@NotNull
	NamedMethod getTimeoutMethod();


	/**
	 * Returns the list of timer children.
	 * @return the list of timer children.
	 */
	@NotNull
	List<Timer> getTimers();
	/**
	 * Adds new child to the list of timer children.
	 * @return created child
	 */
	Timer addTimer();


	/**
	 * Returns the value of the init-on-startup child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:init-on-startup documentation</h3>
	 * The init-on-startup element specifies that a Singleton
	 * 	    bean has eager initialization.
	 * 	    This element can only be specified for singleton session
	 * 	    beans.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the init-on-startup child.
	 */
	@NotNull
	GenericDomValue<Boolean> getInitOnStartup();


	/**
	 * Returns the value of the concurrency-management-type child.
	 * @return the value of the concurrency-management-type child.
	 */
	@NotNull
	GenericDomValue<ConcurrencyManagementType> getConcurrencyManagementType();


	/**
	 * Returns the list of concurrent-method children.
	 * @return the list of concurrent-method children.
	 */
	@NotNull
	List<ConcurrentMethod> getConcurrentMethods();
	/**
	 * Adds new child to the list of concurrent-method children.
	 * @return created child
	 */
	ConcurrentMethod addConcurrentMethod();


	/**
	 * Returns the value of the depends-on child.
	 * @return the value of the depends-on child.
	 */
	@NotNull
	DependsOn getDependsOn();


	/**
	 * Returns the list of init-method children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:init-method documentation</h3>
	 * The init-method element specifies the mappings for
	 * 	    EJB 2.x style create methods for an EJB 3.x bean.
	 * 	    This element can only be specified for stateful
	 *             session beans.
	 * </pre>
	 * @return the list of init-method children.
	 */
	@NotNull
	List<InitMethod> getInitMethods();
	/**
	 * Adds new child to the list of init-method children.
	 * @return created child
	 */
	InitMethod addInitMethod();


	/**
	 * Returns the list of remove-method children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:remove-method documentation</h3>
	 * The remove-method element specifies the mappings for
	 * 	    EJB 2.x style remove methods for an EJB 3.x bean.
	 * 	    This element can only be specified for stateful
	 *             session beans.
	 * </pre>
	 * @return the list of remove-method children.
	 */
	@NotNull
	List<RemoveMethod> getRemoveMethods();
	/**
	 * Adds new child to the list of remove-method children.
	 * @return created child
	 */
	RemoveMethod addRemoveMethod();


	/**
	 * Returns the list of async-method children.
	 * @return the list of async-method children.
	 */
	@NotNull
	List<AsyncMethod> getAsyncMethods();
	/**
	 * Adds new child to the list of async-method children.
	 * @return created child
	 */
	AsyncMethod addAsyncMethod();


	/**
	 * Returns the value of the transaction-type child.
	 * @return the value of the transaction-type child.
	 */
	@NotNull
	GenericDomValue<TransactionType> getTransactionType();


	/**
	 * Returns the value of the after-begin-method child.
	 * @return the value of the after-begin-method child.
	 */
	@NotNull
	NamedMethod getAfterBeginMethod();


	/**
	 * Returns the value of the before-completion-method child.
	 * @return the value of the before-completion-method child.
	 */
	@NotNull
	NamedMethod getBeforeCompletionMethod();


	/**
	 * Returns the value of the after-completion-method child.
	 * @return the value of the after-completion-method child.
	 */
	@NotNull
	NamedMethod getAfterCompletionMethod();


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
	 * Returns the list of security-role-ref children.
	 * @return the list of security-role-ref children.
	 */
	@NotNull
	List<SecurityRoleRef> getSecurityRoleRefs();
	/**
	 * Adds new child to the list of security-role-ref children.
	 * @return created child
	 */
	SecurityRoleRef addSecurityRoleRef();


	/**
	 * Returns the value of the security-identity child.
	 * @return the value of the security-identity child.
	 */
	@NotNull
	SecurityIdentity getSecurityIdentity();


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
