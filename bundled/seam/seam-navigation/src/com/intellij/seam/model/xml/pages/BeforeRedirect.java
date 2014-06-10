package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:before-redirectAttrType enumeration.
 */
public enum BeforeRedirect implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private BeforeRedirect(String value) { this.value = value; }
	public String getValue() { return value; }

}
