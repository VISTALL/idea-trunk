// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:propertyType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:propertyType documentation</h3>
 * Specifies a name/value pair.
 * </pre>
 */
public interface Property extends CommonDomModelElement {

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getName();


	/**
	 * Returns the value of the value child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:xsdStringType documentation</h3>
	 * This type adds an "id" attribute to xsd:string.
	 * </pre>
	 * @return the value of the value child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getValue();


}
