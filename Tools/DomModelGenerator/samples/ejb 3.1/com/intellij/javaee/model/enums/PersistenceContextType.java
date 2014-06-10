// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:persistence-context-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:persistence-context-typeType documentation</h3>
 * The persistence-context-typeType specifies the transactional
 * 	nature of a persistence context reference.
 * 	The value of the persistence-context-type element must be
 * 	one of the following:
 * 	    Transactional
 *             Extended
 * </pre>
 */
public enum PersistenceContextType implements com.intellij.util.xml.NamedEnum {
	EXTENDED ("Extended"),
	TRANSACTIONAL ("Transactional");

	private final String value;
	private PersistenceContextType(String value) { this.value = value; }
	public String getValue() { return value; }

}
