package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:nestedAttrType enumeration.
 */
public enum Nested implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private Nested(String value) { this.value = value; }
	public String getValue() { return value; }

}
