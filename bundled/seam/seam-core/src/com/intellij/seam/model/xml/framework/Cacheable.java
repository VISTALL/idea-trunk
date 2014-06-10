// Generated on Tue Dec 11 18:29:45 MSK 2007
// DTD/Schema  :    http://jboss.com/products/seam/framework

package com.intellij.seam.model.xml.framework;

/**
 * http://jboss.com/products/seam/framework:cacheableAttrType enumeration.
 */
public enum Cacheable implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private Cacheable(String value) { this.value = value; }
	public String getValue() { return value; }

}
