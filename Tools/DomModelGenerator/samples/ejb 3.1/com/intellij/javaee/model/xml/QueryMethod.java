// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.MethodParams;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:query-methodType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:query-methodType documentation</h3>
 * 	  The query-method specifies the method for a finder or select
 * 	  query.
 * 	  The method-name element specifies the name of a finder or select
 * 	  method in the entity bean's implementation class.
 * 	  Each method-param must be defined for a query-method using the
 * 	  method-params element.
 * 	  It is used by the query-method element.
 * 	  Example:
 * 	  <query>
 * 	      <description>Method finds large orders</description>
 * 	      <query-method>
 * 		  <method-name>findLargeOrders</method-name>
 * 		  <method-params></method-params>
 * 	      </query-method>
 * 	      <ejb-ql>
 * 		SELECT OBJECT(o) FROM Order o
 * 		  WHERE o.amount &gt; 1000
 * 	      </ejb-ql>
 * 	  </query>
 * 	  
 * </pre>
 */
public interface QueryMethod extends CommonDomModelElement {

	/**
	 * Returns the value of the method-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:method-nameType documentation</h3>
	 * The method-nameType contains a name of an enterprise
	 * 	bean method or the asterisk (*) character. The asterisk is
	 * 	used when the element denotes all the methods of an
	 * 	enterprise bean's client view interfaces.
	 * </pre>
	 * @return the value of the method-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getMethodName();


	/**
	 * Returns the value of the method-params child.
	 * @return the value of the method-params child.
	 */
	@NotNull
	@Required
	MethodParams getMethodParams();


}
