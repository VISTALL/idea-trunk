// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:activation-config-propertyType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:activation-config-propertyType documentation</h3>
 * The activation-config-propertyType contains a name/value
 * 	configuration property pair for a message-driven bean.
 * 	The properties that are recognized for a particular
 * 	message-driven bean are determined by the messaging type.
 * </pre>
 */
public interface ActivationConfigProperty extends CommonDomModelElement {

	/**
	 * Returns the value of the activation-config-property-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:activation-config-property-name documentation</h3>
	 * The activation-config-property-name element contains
	 * 	    the name for an activation configuration property of
	 * 	    a message-driven bean.
	 * 	    For JMS message-driven beans, the following property
	 * 	    names are recognized: acknowledgeMode,
	 * 	    messageSelector, destinationType, subscriptionDurability
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the activation-config-property-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getActivationConfigPropertyName();


	/**
	 * Returns the value of the activation-config-property-value child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:activation-config-property-value documentation</h3>
	 * The activation-config-property-value element
	 * 	    contains the value for an activation configuration
	 * 	    property of a message-driven bean.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the activation-config-property-value child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getActivationConfigPropertyValue();


}
