// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.MethodIntf;
import com.intellij.javaee.model.xml.ejb.MethodParams;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:async-methodType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:async-methodType documentation</h3>
 * The async-methodType element specifies that a session
 * 	bean method has asynchronous invocation semantics.
 * 	The optional method-intf element constrains the async
 * 	method behavior to the client views of the given method-intf
 * 	type.  This value must be either Remote or Local.
 * </pre>
 */
public interface AsyncMethod extends CommonDomModelElement {

	/**
	 * Returns the value of the method-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
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
	MethodParams getMethodParams();


	/**
	 * Returns the value of the method-intf child.
	 * @return the value of the method-intf child.
	 */
	@NotNull
	GenericDomValue<MethodIntf> getMethodIntf();


}
