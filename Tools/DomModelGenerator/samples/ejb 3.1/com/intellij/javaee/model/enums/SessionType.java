// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:session-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:session-typeType documentation</h3>
 * The session-typeType describes whether the session bean is a
 * 	singleton, stateful or stateless session. It is used by
 * 	session-type elements.
 * 	The value must be one of the three following:
 * 	    Singleton
 * 	    Stateful
 * 	    Stateless
 * </pre>
 */
public enum SessionType implements com.intellij.util.xml.NamedEnum {
	SINGLETON ("Singleton"),
	STATEFUL ("Stateful"),
	STATELESS ("Stateless");

	private final String value;
	private SessionType(String value) { this.value = value; }
	public String getValue() { return value; }

}
