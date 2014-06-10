package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:severityAttrType enumeration.
 */
public enum Severity implements com.intellij.util.xml.NamedEnum {
	ERROR ("ERROR"),
	FATAL ("FATAL"),
	INFO ("INFO"),
	WARN ("WARN"),
	ERROR_LOWCASE("error"),
	FATAL_LOWCASE("fatal"),
	INFO_LOWCASE("info"),
	WARN_LOWCASE("warn");

	private final String value;
	private Severity(String value) { this.value = value; }
	public String getValue() { return value; }

}
