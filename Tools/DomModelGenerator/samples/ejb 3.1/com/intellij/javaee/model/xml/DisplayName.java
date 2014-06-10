// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:display-nameType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:display-nameType documentation</h3>
 * 	  The display-name type contains a short name that is intended
 * 	  to be displayed by tools. It is used by display-name
 * 	  elements.  The display name need not be unique.
 * 	  Example:
 * 	  ...
 * 	     <display-name xml:lang="en">
 * 	       Employee Self Service
 * 	     </display-name>
 * 	  The value of the xml:lang attribute is "en" (English) by default.
 * 	  
 * </pre>
 */
public interface DisplayName extends CommonDomModelElement {

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
