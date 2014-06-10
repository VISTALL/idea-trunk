// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:param-valueType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:param-valueType documentation</h3>
 * This type is a general type that can be used to declare
 * 	parameter/value lists.
 * </pre>
 */
public interface ParamValue extends CommonDomModelElement {

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
	 * Returns the value of the param-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:param-name documentation</h3>
	 * The param-name element contains the name of a
	 * 	    parameter.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the param-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getParamName();


	/**
	 * Returns the value of the param-value child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:param-value documentation</h3>
	 * The param-value element contains the value of a
	 * 	    parameter.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the param-value child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getParamValue();


}
