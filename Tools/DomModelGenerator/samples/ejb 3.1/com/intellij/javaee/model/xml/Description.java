// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:descriptionType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:descriptionType documentation</h3>
 * The description type is used by a description element to
 * 	provide text describing the parent element.  The elements
 * 	that use this type should include any information that the
 * 	Deployment Component's Deployment File file producer wants
 * 	to provide to the consumer of the Deployment Component's
 * 	Deployment File (i.e., to the Deployer). Typically, the
 * 	tools used by such a Deployment File consumer will display
 * 	the description when processing the parent element that
 * 	contains the description.
 * 	The lang attribute defines the language that the
 * 	description is provided in. The default value is "en" (English).
 * </pre>
 */
public interface Description extends CommonDomModelElement {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	@Required
	String getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(@NotNull String value);


	/**
	 * Returns the value of the lang child.
	 * @return the value of the lang child.
	 */
	@NotNull
	GenericAttributeValue<String> getLang();


}
