// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

/**
 * http://www.springframework.org/schema/context:scoped-proxyAttrType enumeration.
 */
public enum ScopedProxy implements com.intellij.util.xml.NamedEnum {
	INTERFACES ("interfaces"),
	NO ("no"),
	TARGET_CLASS ("targetClass");

	private final String value;
	private ScopedProxy(String value) { this.value = value; }
	public String getValue() { return value; }

}
