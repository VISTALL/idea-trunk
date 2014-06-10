package com.intellij.seam.model.xml.pages;

import com.intellij.util.xml.NamedEnum;

/**
 * http://jboss.com/products/seam/pages:switchAttrType enumeration.
 */
public enum Switch implements NamedEnum {
	DISABLED ("disabled"),
	ENABLED ("enabled");

	private final String value;
	private Switch(String value) { this.value = value; }
	public String getValue() { return value; }
}
