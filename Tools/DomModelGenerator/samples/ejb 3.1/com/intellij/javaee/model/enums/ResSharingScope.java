// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:res-sharing-scopeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:res-sharing-scopeType documentation</h3>
 * The res-sharing-scope type specifies whether connections
 * 	obtained through the given resource manager connection
 * 	factory reference can be shared. The value, if specified,
 * 	must be one of the two following:
 * 	    Shareable
 * 	    Unshareable
 * 	The default value is Shareable.
 * </pre>
 */
public enum ResSharingScope implements com.intellij.util.xml.NamedEnum {
	SHAREABLE ("Shareable"),
	UNSHAREABLE ("Unshareable");

	private final String value;
	private ResSharingScope(String value) { this.value = value; }
	public String getValue() { return value; }

}
