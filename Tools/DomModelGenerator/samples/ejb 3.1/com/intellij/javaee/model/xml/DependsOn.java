// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:depends-onType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:depends-onType documentation</h3>
 * The depends-onType is used to express initialization
 * 	ordering dependencies between Singleton components.
 * 	The depends-onType specifies the names of one or more
 * 	Singleton beans in the same application as the referring
 * 	Singleton, each of which must be initialized before
 * 	the referring bean.
 * 	Each dependent bean is expressed using ejb-link syntax.
 * 	The order in which dependent beans are initialized at
 * 	runtime is not guaranteed to match the order in which
 * 	they are listed.
 * </pre>
 */
public interface DependsOn extends CommonDomModelElement {

	/**
	 * Returns the list of ejb-name children.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:ejb-linkType documentation</h3>
	 * 	  The ejb-linkType is used by ejb-link
	 * 	  elements in the ejb-ref or ejb-local-ref elements to specify
	 * 	  that an EJB reference is linked to enterprise bean.
	 * 	  The value of the ejb-link element must be the ejb-name of an
	 * 	  enterprise bean in the same ejb-jar file or in another ejb-jar
	 * 	  file in the same Java EE application unit.
	 * 	  Alternatively, the name in the ejb-link element may be
	 * 	  composed of a path name specifying the ejb-jar containing the
	 * 	  referenced enterprise bean with the ejb-name of the target
	 * 	  bean appended and separated from the path name by "#".  The
	 * 	  path name is relative to the Deployment File containing
	 * 	  Deployment Component that is referencing the enterprise
	 * 	  bean.  This allows multiple enterprise beans with the same
	 * 	  ejb-name to be uniquely identified.
	 * 	  Examples:
	 * 	      <ejb-link>EmployeeRecord</ejb-link>
	 * 	      <ejb-link>../products/product.jar#ProductEJB</ejb-link>
	 * 	  
	 * </pre>
	 * @return the list of ejb-name children.
	 */
	@NotNull
	@Required
	List<GenericDomValue<String>> getEjbNames();
	/**
	 * Adds new child to the list of ejb-name children.
	 * @return created child
	 */
	GenericDomValue<String> addEjbName();


}
