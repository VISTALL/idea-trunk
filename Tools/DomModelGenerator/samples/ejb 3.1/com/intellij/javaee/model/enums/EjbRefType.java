// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:ejb-ref-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:ejb-ref-typeType documentation</h3>
 * The ejb-ref-typeType contains the expected type of the
 * 	referenced enterprise bean.
 * 	The ejb-ref-type designates a value
 * 	that must be one of the following:
 * 	    Entity
 * 	    Session
 * </pre>
 */
public enum EjbRefType implements com.intellij.util.xml.NamedEnum {
	ENTITY ("Entity"),
	SESSION ("Session");

	private final String value;
	private EjbRefType(String value) { this.value = value; }
	public String getValue() { return value; }

}
