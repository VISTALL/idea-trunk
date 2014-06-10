// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

/**
 * http://www.springframework.org/schema/aop:typeAttrType enumeration.
 */
public enum PointcutType implements com.intellij.util.xml.NamedEnum {
	ASPECTJ ("aspectj"),
	REGEX ("regex");

	private final String value;
	private PointcutType(String value) { this.value = value; }
	public String getValue() { return value; }

}
