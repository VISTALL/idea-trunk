// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:res-authType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:res-authType documentation</h3>
 * The res-authType specifies whether the Deployment Component
 * 	code signs on programmatically to the resource manager, or
 * 	whether the Container will sign on to the resource manager
 * 	on behalf of the Deployment Component. In the latter case,
 * 	the Container uses information that is supplied by the
 * 	Deployer.
 * 	The value must be one of the two following:
 * 	    Application
 * 	    Container
 * </pre>
 */
public enum ResAuth implements com.intellij.util.xml.NamedEnum {
	APPLICATION ("Application"),
	CONTAINER ("Container");

	private final String value;
	private ResAuth(String value) { this.value = value; }
	public String getValue() { return value; }

}
