// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:concurrency-management-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:concurrency-management-typeType documentation</h3>
 * The concurrency-management-typeType specifies the way concurrency
 * 	is managed for a singleton or stateful session bean.
 * 	The concurrency management type must be one of the following:
 * 	    Bean
 * 	    Container
 * 	    NotAllowed
 * 	Bean managed concurrency can only be specified for a singleton bean.
 * </pre>
 */
public enum ConcurrencyManagementType implements com.intellij.util.xml.NamedEnum {
	BEAN ("Bean"),
	CONTAINER ("Container"),
	NOT_ALLOWED ("NotAllowed");

	private final String value;
	private ConcurrencyManagementType(String value) { this.value = value; }
	public String getValue() { return value; }

}
