// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.TimeUnitType;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:stateful-timeoutType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:stateful-timeoutType documentation</h3>
 * The stateful-timeoutType represents the amount of time
 * 	a stateful session bean can be idle(not receive any client
 * 	invocations) before it is eligible for removal by the container.
 * </pre>
 */
public interface StatefulTimeout extends CommonDomModelElement {

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
