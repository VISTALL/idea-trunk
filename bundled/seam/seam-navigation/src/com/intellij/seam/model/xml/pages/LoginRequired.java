package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:login-requiredAttrType enumeration.
 */
public enum LoginRequired implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private LoginRequired(String value) { this.value = value; }
	public String getValue() { return value; }

}
