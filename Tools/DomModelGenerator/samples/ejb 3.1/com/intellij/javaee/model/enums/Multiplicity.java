// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:multiplicityType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:multiplicityType documentation</h3>
 * The multiplicityType describes the multiplicity of the
 * 	role that participates in a relation.
 * 	The value must be one of the two following:
 * 	    One
 * 	    Many
 * </pre>
 */
public enum Multiplicity implements com.intellij.util.xml.NamedEnum {
	MANY ("Many"),
	ONE ("One");

	private final String value;
	private Multiplicity(String value) { this.value = value; }
	public String getValue() { return value; }

}
