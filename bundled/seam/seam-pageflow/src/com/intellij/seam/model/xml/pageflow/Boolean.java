package com.intellij.seam.model.xml.pageflow;

/**
 * http://jboss.com/products/seam/pageflow:booleanType enumeration.
 */
public enum Boolean implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	NO ("no"),
	OFF ("off"),
	ON ("on"),
	TRUE ("true"),
	YES ("yes");

	private final String value;
	private Boolean(String value) { this.value = value; }
	public String getValue() { return value; }

}
