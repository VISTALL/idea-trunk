// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:trans-attributeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:trans-attributeType documentation</h3>
 * The trans-attributeType specifies how the container must
 * 	manage the transaction boundaries when delegating a method
 * 	invocation to an enterprise bean's business method.
 * 	The value must be one of the following:
 * 	    NotSupported
 * 	    Supports
 * 	    Required
 * 	    RequiresNew
 * 	    Mandatory
 * 	    Never
 * </pre>
 */
public enum TransAttribute implements com.intellij.util.xml.NamedEnum {
	MANDATORY ("Mandatory"),
	NEVER ("Never"),
	NOT_SUPPORTED ("NotSupported"),
	REQUIRED ("Required"),
	REQUIRES_NEW ("RequiresNew"),
	SUPPORTS ("Supports");

	private final String value;
	private TransAttribute(String value) { this.value = value; }
	public String getValue() { return value; }

}
