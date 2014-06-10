// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:persistence-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:persistence-typeType documentation</h3>
 * The persistence-typeType specifies an entity bean's persistence
 * 	management type.
 * 	The persistence-type element must be one of the two following:
 * 	    Bean
 * 	    Container
 * </pre>
 */
public enum PersistenceType implements com.intellij.util.xml.NamedEnum {
	BEAN ("Bean"),
	CONTAINER ("Container");

	private final String value;
	private PersistenceType(String value) { this.value = value; }
	public String getValue() { return value; }

}
