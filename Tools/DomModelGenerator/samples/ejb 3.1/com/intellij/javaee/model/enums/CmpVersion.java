// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:cmp-versionType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:cmp-versionType documentation</h3>
 * The cmp-versionType specifies the version of an entity bean
 * 	with container-managed persistence. It is used by
 * 	cmp-version elements.
 * 	The value must be one of the two following:
 * 	    1.x
 * 	    2.x
 * </pre>
 */
public enum CmpVersion implements com.intellij.util.xml.NamedEnum {
	CmpVersion_1_X ("1.x"),
	CmpVersion_2_X ("2.x");

	private final String value;
	private CmpVersion(String value) { this.value = value; }
	public String getValue() { return value; }

}
