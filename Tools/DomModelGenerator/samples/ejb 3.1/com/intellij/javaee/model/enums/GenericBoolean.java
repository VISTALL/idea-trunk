// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:generic-booleanType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:generic-booleanType documentation</h3>
 * This type defines four different values which can designate
 * 	boolean values. This includes values yes and no which are
 * 	not designated by xsd:boolean
 * </pre>
 */
public enum GenericBoolean implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	NO ("no"),
	TRUE ("true"),
	YES ("yes");

	private final String value;
	private GenericBoolean(String value) { this.value = value; }
	public String getValue() { return value; }

}
