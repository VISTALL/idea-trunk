// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

/**
 * http://www.springframework.org/schema/beans:default-dependency-checkAttrType enumeration.
 */
public enum DefaultDependencyCheck implements com.intellij.util.xml.NamedEnum {
	ALL ("all"),
	NONE ("none"),
	OBJECTS ("objects"),
	SIMPLE ("simple");

	private final String value;
	private DefaultDependencyCheck(String value) { this.value = value; }
	public String getValue() { return value; }

}
