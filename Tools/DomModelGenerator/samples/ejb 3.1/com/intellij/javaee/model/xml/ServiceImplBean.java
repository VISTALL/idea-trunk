// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:service-impl-beanType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:service-impl-beanType documentation</h3>
 * The service-impl-bean element defines the web service implementation.
 * 	A service implementation can be an EJB bean class or JAX-RPC web
 * 	component.  Existing EJB implementations are exposed as a web service
 * 	using an ejb-link.
 * 	Used in: port-component
 * </pre>
 */
public interface ServiceImplBean extends CommonDomModelElement {

	/**
	 * Returns the value of the ejb-link child.
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
	 * @return the value of the ejb-link child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getEjbLink();


	/**
	 * Returns the value of the servlet-link child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:servlet-linkType documentation</h3>
	 * 	  The servlet-link element is used in the service-impl-bean element
	 * 	  to specify that a Service Implementation Bean is defined as a
	 * 	  JAX-RPC Service Endpoint.
	 * 	  The value of the servlet-link element must be the servlet-name of
	 * 	  a JAX-RPC Service Endpoint in the same WAR file.
	 * 	  Used in: service-impl-bean
	 * 	  Example:
	 * 		  <servlet-link>StockQuoteService</servlet-link>
	 * 	  
	 * </pre>
	 * @return the value of the servlet-link child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getServletLink();


}
