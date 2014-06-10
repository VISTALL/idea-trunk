
package com.intellij.seam.model.xml.pageflow;

/**
 * http://jboss.com/products/seam/pageflow:config-typeAttrType enumeration.
 */
public enum ConfigType implements com.intellij.util.xml.NamedEnum {
	BEAN ("bean"),
	CONFIGURATION_PROPERTY ("configuration-property"),
	CONSTRUCTOR ("constructor"),
	FIELD ("field");

	private final String value;
	private ConfigType(String value) { this.value = value; }
	public String getValue() { return value; }

}
