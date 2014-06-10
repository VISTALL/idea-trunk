// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:cmp-fieldType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:cmp-fieldType documentation</h3>
 * The cmp-fieldType describes a container-managed field. The
 * 	cmp-fieldType contains an optional description of the field,
 * 	and the name of the field.
 * </pre>
 */
public interface CmpField extends CommonDomModelElement {

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
	 * Returns the value of the field-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:field-name documentation</h3>
	 * The field-name element specifies the name of a
	 * 	    container managed field.
	 * 	    The name of the cmp-field of an entity bean with
	 * 	    cmp-version 2.x must begin with a lowercase
	 * 	    letter. This field is accessed by methods whose
	 * 	    names consists of the name of the field specified by
	 * 	    field-name in which the first letter is uppercased,
	 * 	    prefixed by "get" or "set".
	 * 	    The name of the cmp-field of an entity bean with
	 * 	    cmp-version 1.x must denote a public field of the
	 * 	    enterprise bean class or one of its superclasses.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:java-identifierType documentation</h3>
	 * The java-identifierType defines a Java identifier.
	 * 	The users of this type should further verify that
	 * 	the content does not contain Java reserved keywords.
	 * </pre>
	 * @return the value of the field-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getFieldName();


}
