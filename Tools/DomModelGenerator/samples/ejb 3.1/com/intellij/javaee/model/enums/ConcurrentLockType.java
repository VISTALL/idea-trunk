// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:concurrent-lock-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:concurrent-lock-typeType documentation</h3>
 * The concurrent-lock-typeType specifies how the container must
 * 	manage concurrent access to a method of a Singleton bean
 * 	with container-managed concurrency.
 * 	The container managed concurrency lock type must be one
 * 	of the following :
 * 	    Read
 * 	    Write
 * </pre>
 */
public enum ConcurrentLockType implements com.intellij.util.xml.NamedEnum {
	READ ("Read"),
	WRITE ("Write");

	private final String value;
	private ConcurrentLockType(String value) { this.value = value; }
	public String getValue() { return value; }

}
