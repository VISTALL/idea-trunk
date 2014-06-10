// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.TimeUnitType;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:access-timeoutType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:access-timeoutType documentation</h3>
 * The access-timeoutType represents the maximum amount of
 * 	time (in a given time unit) that the container should wait for
 * 	a concurrency lock before throwing a timeout exception to the
 * 	client.
 * </pre>
 */
public interface AccessTimeout extends CommonDomModelElement {

	/**
	 * Returns the value of the timeout child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdPositiveIntegerType documentation</h3>
	 * This type adds an "id" attribute to xsd:positiveInteger.
	 * </pre>
	 * @return the value of the timeout child.
	 */
	@NotNull
	@Required
	GenericDomValue<Integer> getTimeout();


	/**
	 * Returns the value of the unit child.
	 * @return the value of the unit child.
	 */
	@NotNull
	@Required
	GenericDomValue<TimeUnitType> getUnit();


}
