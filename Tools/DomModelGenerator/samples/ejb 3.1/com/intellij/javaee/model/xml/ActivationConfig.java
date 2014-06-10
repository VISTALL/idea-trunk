// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.ActivationConfigProperty;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:activation-configType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:activation-configType documentation</h3>
 * The activation-configType defines information about the
 * 	expected configuration properties of the message-driven bean
 * 	in its operational environment. This may include information
 * 	about message acknowledgement, message selector, expected
 * 	destination type, etc.
 * 	The configuration information is expressed in terms of
 * 	name/value configuration properties.
 * 	The properties that are recognized for a particular
 * 	message-driven bean are determined by the messaging type.
 * </pre>
 */
public interface ActivationConfig extends CommonDomModelElement {

	/**
	 * Returns the list of description children.
	 * @return the list of description children.
	 */
	@NotNull
	List<Description> getDescriptions();
	/**
	 * Adds new child to the list of description children.
	 * @return created child
	 */
	Description addDescription();


	/**
	 * Returns the list of activation-config-property children.
	 * @return the list of activation-config-property children.
	 */
	@NotNull
	@Required
	List<ActivationConfigProperty> getActivationConfigProperties();
	/**
	 * Adds new child to the list of activation-config-property children.
	 * @return created child
	 */
	ActivationConfigProperty addActivationConfigProperty();


}
