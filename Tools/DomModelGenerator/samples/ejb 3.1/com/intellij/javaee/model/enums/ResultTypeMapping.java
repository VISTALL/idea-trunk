// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:result-type-mappingType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:result-type-mappingType documentation</h3>
 * The result-type-mappingType is used in the query element to
 * 	specify whether an abstract schema type returned by a query
 * 	for a select method is to be mapped to an EJBLocalObject or
 * 	EJBObject type.
 * 	The value must be one of the following:
 * 	    Local
 * 	    Remote
 * </pre>
 */
public enum ResultTypeMapping implements com.intellij.util.xml.NamedEnum {
	LOCAL ("Local"),
	REMOTE ("Remote");

	private final String value;
	private ResultTypeMapping(String value) { this.value = value; }
	public String getValue() { return value; }

}
