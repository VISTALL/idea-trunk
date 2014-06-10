// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:time-unit-typeType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:time-unit-typeType documentation</h3>
 * The time-unit-typeType represents a time duration at a given
 * 	unit of granularity.
 * 	The time unit type must be one of the following :
 * 	    Days
 * 	    Hours
 * 	    Minutes
 * 	    Seconds
 * 	    Milliseconds
 * 	    Microseconds
 * 	    Nanoseconds
 * </pre>
 */
public enum TimeUnitType implements com.intellij.util.xml.NamedEnum {
	DAYS ("Days"),
	HOURS ("Hours"),
	MICROSECONDS ("Microseconds"),
	MILLISECONDS ("Milliseconds"),
	MINUTES ("Minutes"),
	NANOSECONDS ("Nanoseconds"),
	SECONDS ("Seconds");

	private final String value;
	private TimeUnitType(String value) { this.value = value; }
	public String getValue() { return value; }

}
