// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

/**
 * http://www.springframework.org/schema/context:typeAttrType enumeration.
 */
public enum Type implements com.intellij.util.xml.NamedEnum {
	ANNOTATION ("annotation"),
	ASPECTJ ("aspectj"),
	ASSIGNABLE ("assignable"),
	REGEX ("regex");

	private final String value;
	private Type(String value) { this.value = value; }
	public String getValue() { return value; }

}
