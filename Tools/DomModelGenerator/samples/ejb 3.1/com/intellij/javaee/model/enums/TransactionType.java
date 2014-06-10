// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:transaction-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:transaction-typeType documentation</h3>
 * The transaction-typeType specifies an enterprise bean's
 * 	transaction management type.
 * 	The transaction-type must be one of the two following:
 * 	    Bean
 * 	    Container
 * </pre>
 */
public enum TransactionType implements com.intellij.util.xml.NamedEnum {
	BEAN ("Bean"),
	CONTAINER ("Container");

	private final String value;
	private TransactionType(String value) { this.value = value; }
	public String getValue() { return value; }

}
