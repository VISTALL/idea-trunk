// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tool

package com.intellij.spring.model.xml.tool;

/**
 * http://www.springframework.org/schema/tool:kindAttrType enumeration.
 */
public enum SpringAnnotationKind implements com.intellij.util.xml.NamedEnum {
	DIRECT ("direct"),
	REF ("ref");

	private final String value;
	private SpringAnnotationKind(String value) { this.value = value; }
	public String getValue() { return value; }

}
