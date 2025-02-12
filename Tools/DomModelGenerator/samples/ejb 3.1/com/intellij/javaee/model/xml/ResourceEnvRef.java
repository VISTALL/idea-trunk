// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:resource-env-refType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:resource-env-refType documentation</h3>
 * 	  The resource-env-refType is used to define
 * 	  resource-env-type elements.  It contains a declaration of a
 * 	  Deployment Component's reference to an administered object
 * 	  associated with a resource in the Deployment Component's
 * 	  environment.  It consists of an optional description, the
 * 	  resource environment reference name, and an optional
 * 	  indication of the resource environment reference type
 * 	  expected by the Deployment Component code.
 * 	  It also includes optional elements to define injection of
 * 	  the named resource into fields or JavaBeans properties.
 * 	  The resource environment type must be supplied unless an
 * 	  injection target is specified, in which case the type
 * 	  of the target is used.  If both are specified, the type
 * 	  must be assignment compatible with the type of the injection
 * 	  target.
 * 	  Example:
 * 	  <resource-env-ref>
 * 	      <resource-env-ref-name>jms/StockQueue
 * 	      </resource-env-ref-name>
 * 	      <resource-env-ref-type>javax.jms.Queue
 * 	      </resource-env-ref-type>
 * 	  </resource-env-ref>
 * 	  
 * </pre>
 */
public interface ResourceEnvRef extends CommonDomModelElement, ResourceGroup {

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
	 * Returns the value of the resource-env-ref-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:resource-env-ref-name documentation</h3>
	 * The resource-env-ref-name element specifies the name
	 * 	    of a resource environment reference; its value is
	 * 	    the environment entry name used in
	 * 	    the Deployment Component code.  The name is a JNDI
	 * 	    name relative to the java:comp/env context and must
	 * 	    be unique within a Deployment Component.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:jndi-nameType documentation</h3>
	 * The jndi-nameType type designates a JNDI name in the
	 * 	Deployment Component's environment and is relative to the
	 * 	java:comp/env context.  A JNDI name must be unique within the
	 * 	Deployment Component.
	 * </pre>
	 * @return the value of the resource-env-ref-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getResourceEnvRefName();


	/**
	 * Returns the value of the resource-env-ref-type child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:resource-env-ref-type documentation</h3>
	 * The resource-env-ref-type element specifies the type
	 * 	    of a resource environment reference.  It is the
	 * 	    fully qualified name of a Java language class or
	 * 	    interface.
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
	 * @return the value of the resource-env-ref-type child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getResourceEnvRefType();


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
