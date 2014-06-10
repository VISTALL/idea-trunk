// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.enums;

/**
 * http://java.sun.com/xml/ns/javaee:message-destination-usageType enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/javaee:message-destination-usageType documentation</h3>
 * The message-destination-usageType specifies the use of the
 * 	message destination indicated by the reference.  The value
 * 	indicates whether messages are consumed from the message
 * 	destination, produced for the destination, or both.  The
 * 	Assembler makes use of this information in linking producers
 * 	of a destination with its consumers.
 * 	The value of the message-destination-usage element must be
 * 	one of the following:
 * 	    Consumes
 * 	    Produces
 * 	    ConsumesProduces
 * </pre>
 */
public enum MessageDestinationUsage implements com.intellij.util.xml.NamedEnum {
	CONSUMES ("Consumes"),
	CONSUMES_PRODUCES ("ConsumesProduces"),
	PRODUCES ("Produces");

	private final String value;
	private MessageDestinationUsage(String value) { this.value = value; }
	public String getValue() { return value; }

}
