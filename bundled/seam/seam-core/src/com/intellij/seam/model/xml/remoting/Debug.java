// Generated on Tue Dec 11 18:29:45 MSK 2007
// DTD/Schema  :    http://jboss.com/products/seam/remoting

package com.intellij.seam.model.xml.remoting;

/**
 * http://jboss.com/products/seam/remoting:debugAttrType enumeration.
 */
public enum Debug implements com.intellij.util.xml.NamedEnum {
	FALSE ("false"),
	TRUE ("true");

	private final String value;
	private Debug(String value) { this.value = value; }
	public String getValue() { return value; }

}
