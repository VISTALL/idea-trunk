// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:cmr-field-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:cmr-field-typeType documentation</h3>
 * The cmr-field-type element specifies the class of a
 * 	collection-valued logical relationship field in the entity
 * 	bean class. The value of an element using cmr-field-typeType
 * 	must be either: java.util.Collection or java.util.Set.
 * </pre>
 */
public enum CmrFieldType implements com.intellij.util.xml.NamedEnum {
	JAVA_UTIL_COLLECTION ("java.util.Collection"),
	JAVA_UTIL_SET ("java.util.Set");

	private final String value;
	private CmrFieldType(String value) { this.value = value; }
	public String getValue() { return value; }

}
