package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:flush-modeAttrType enumeration.
 */
public enum FlushMode implements com.intellij.util.xml.NamedEnum {
	AUTO ("AUTO"),
	COMMIT ("COMMIT"),
	MANUAL ("MANUAL"),
	AUTO_LOWCASE ("auto"),
	COMMIT_LOWCASEException ("commit"),
	MANUAL_LOWCASE ("manual");

	private final String value;
	private FlushMode(String value) { this.value = value; }
	public String getValue() { return value; }

}
