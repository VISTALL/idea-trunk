// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:message-destinationType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:message-destinationType documentation</h3>
 * 	  The message-destinationType specifies a message
 * 	  destination. The logical destination described by this
 * 	  element is mapped to a physical destination by the Deployer.
 * 	  The message destination element contains:
 * 		  - an optional description
 * 		  - an optional display-name
 * 		  - an optional icon
 * 		  - a message destination name which must be unique
 * 		    among message destination names within the same
 * 		    Deployment File.
 * 		  - an optional mapped name
 * 	  Example:
 * 	  <message-destination>
 * 		  <message-destination-name>CorporateStocks
 * 		  </message-destination-name>
 * 	  </message-destination>
 * 	  
 * </pre>
 */
public interface MessageDestination extends CommonDomModelElement, DescriptionGroup {

	/**
	 * Returns the value of the message-destination-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:message-destination-name documentation</h3>
	 * The message-destination-name element specifies a
	 * 	    name for a message destination.  This name must be
	 * 	    unique among the names of message destinations
	 * 	    within the Deployment File.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the message-destination-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getMessageDestinationName();


	/**
	 * Returns the value of the mapped-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:mapped-name documentation</h3>
	 * 	      A product specific name that this message destination
	 * 	      should be mapped to.  Each message-destination-ref
	 * 	      element that references this message destination will
	 * 	      define a name in the namespace of the referencing
	 * 	      component.  (It's a name in the JNDI java:comp/env
	 * 	      namespace.)  Many application servers provide a way to
	 * 	      map these local names to names of resources known to the
	 * 	      application server.  This mapped name is often a global
	 * 	      JNDI name, but may be a name of any form.  Each of the
	 * 	      local names should be mapped to this same global name.
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
