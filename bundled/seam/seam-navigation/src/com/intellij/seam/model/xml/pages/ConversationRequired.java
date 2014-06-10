package com.intellij.seam.model.xml.pages;

/**
 * http://jboss.com/products/seam/pages:conversation-requiredAttrType enumeration.
 */
public enum ConversationRequired implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private ConversationRequired(String value) { this.value = value; }
	public String getValue() { return value; }

}
