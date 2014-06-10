// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.EnvEntryTypeValues;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:env-entryType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:env-entryType documentation</h3>
 * The env-entryType is used to declare an application's
 * 	environment entry. The declaration consists of an optional
 * 	description, the name of the environment entry, a type
 * 	(optional if the value is injected, otherwise required), and
 * 	an optional value.
 * 	It also includes optional elements to define injection of
 * 	the named resource into fields or JavaBeans properties.
 * 	If a value is not specified and injection is requested,
 * 	no injection will occur and no entry of the specified name
 * 	will be created.  This allows an initial value to be
 * 	specified in the source code without being incorrectly
 * 	changed when no override has been specified.
 * 	If a value is not specified and no injection is requested,
 * 	a value must be supplied during deployment.
 * 	This type is used by env-entry elements.
 * </pre>
 */
public interface EnvEntry extends CommonDomModelElement, ResourceGroup {

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
	 * Returns the value of the env-entry-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:env-entry-name documentation</h3>
	 * 	      The env-entry-name element contains the name of a
	 * 	      Deployment Component's environment entry.  The name
	 * 	      is a JNDI name relative to the java:comp/env
	 * 	      context.  The name must be unique within a
	 * 	      Deployment Component. The uniqueness
	 * 	      constraints must be defined within the declared
	 * 	      context.
	 * 	      Example:
	 * 	      <env-entry-name>minAmount</env-entry-name>
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:jndi-nameType documentation</h3>
	 * The jndi-nameType type designates a JNDI name in the
	 * 	Deployment Component's environment and is relative to the
	 * 	java:comp/env context.  A JNDI name must be unique within the
	 * 	Deployment Component.
	 * </pre>
	 * @return the value of the env-entry-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getEnvEntryName();


	/**
	 * Returns the value of the env-entry-type child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:env-entry-type documentation</h3>
	 * 	      The env-entry-type element contains the Java language
	 * 	      type of the environment entry.  If an injection target
	 * 	      is specified for the environment entry, the type may
	 * 	      be omitted, or must match the type of the injection
	 * 	      target.  If no injection target is specified, the type
	 * 	      is required.
	 * 	      Example:
	 * 	      <env-entry-type>java.lang.Integer</env-entry-type>
	 * 	      
	 * </pre>
	 * @return the value of the env-entry-type child.
	 */
	@NotNull
	GenericDomValue<EnvEntryTypeValues> getEnvEntryType();


	/**
	 * Returns the value of the env-entry-value child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:env-entry-value documentation</h3>
	 * 	      The env-entry-value designates the value of a
	 * 	      Deployment Component's environment entry. The value
	 * 	      must be a String that is valid for the
	 * 	      constructor of the specified type that takes a
	 * 	      single String parameter, or for java.lang.Character,
	 * 	      a single character.
	 * 	      Example:
	 * 	      <env-entry-value>100.00</env-entry-value>
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the env-entry-value child.
	 */
	@NotNull
	GenericDomValue<String> getEnvEntryValue();


	/**
	 * Returns the value of the mapped-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:mapped-name documentation</h3>
	 * 	      A product specific name that this resource should be
	 * 	      mapped to.  The name of this resource, as defined by the
	 * 	      resource's name element or defaulted, is a name that is
	 * 	      local to the application component using the resource.
	 * 	      (It's a name in the JNDI java:comp/env namespace.)  Many
	 * 	      application servers provide a way to map these local
	 * 	      names to names of resources known to the application
	 * 	      server.  This mapped name is often a global JNDI name,
	 * 	      but may be a name of any form.
	 * 	      Application servers are not required to support any
	 * 	      particular form or type of mapped name, nor the ability
	 * 	      to use mapped names.  The mapped name is
	 * 	      product-dependent and often installation-dependent.  No
	 * 	      use of a mapped name is portable.
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the mapped-name child.
	 */
	@NotNull
	GenericDomValue<String> getMappedName();


	/**
	 * Returns the list of injection-target children.
	 * @return the list of injection-target children.
	 */
	@NotNull
	List<InjectionTarget> getInjectionTargets();
	/**
	 * Adds new child to the list of injection-target children.
	 * @return created child
	 */
	InjectionTarget addInjectionTarget();


}
