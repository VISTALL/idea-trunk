package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:joinAttrType enumeration.
 */
public enum Join implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private Join(String value) { this.value = value; }
	public String getValue() { return value; }

}
