// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/jee

package com.intellij.spring.model.xml.jee;

/**
 * http://www.springframework.org/schema/jee:providerAttrType enumeration.
 */
public enum JeeProvider implements com.intellij.util.xml.NamedEnum {
	HIBERNATE ("hibernate"),
	KODO ("kodo"),
	TOPLINK ("toplink");

	private final String value;
	private JeeProvider(String value) { this.value = value; }
	public String getValue() { return value; }

}
