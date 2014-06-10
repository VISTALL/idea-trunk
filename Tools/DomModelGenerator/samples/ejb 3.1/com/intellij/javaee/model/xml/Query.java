// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.ResultTypeMapping;
import com.intellij.javaee.model.xml.ejb.QueryMethod;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:queryType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:queryType documentation</h3>
 * The queryType defines a finder or select
 * 	query. It contains
 * 	    - an optional description of the query
 * 	    - the specification of the finder or select
 * 	      method it is used by
 * 		- an optional specification of the result type
 * 		  mapping, if the query is for a select method
 * 		  and entity objects are returned.
 * 		- the EJB QL query string that defines the query.
 * 	Queries that are expressible in EJB QL must use the ejb-ql
 * 	element to specify the query. If a query is not expressible
 * 	in EJB QL, the description element should be used to
 * 	describe the semantics of the query and the ejb-ql element
 * 	should be empty.
 * 	The result-type-mapping is an optional element. It can only
 * 	be present if the query-method specifies a select method
 * 	that returns entity objects.  The default value for the
 * 	result-type-mapping element is "Local".
 * </pre>
 */
public interface Query extends CommonDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	Description getDescription();


	/**
	 * Returns the value of the query-method child.
	 * @return the value of the query-method child.
	 */
	@NotNull
	@Required
	QueryMethod getQueryMethod();


	/**
	 * Returns the value of the result-type-mapping child.
	 * @return the value of the result-type-mapping child.
	 */
	@NotNull
	GenericDomValue<ResultTypeMapping> getResultTypeMapping();


	/**
	 * Returns the value of the ejb-ql child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the ejb-ql child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getEjbQl();


}
