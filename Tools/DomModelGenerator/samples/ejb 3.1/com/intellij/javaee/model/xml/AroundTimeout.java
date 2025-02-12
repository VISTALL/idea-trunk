// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:around-timeoutType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:around-timeoutType documentation</h3>
 * The around-timeout type specifies a method on a
 *         class to be called during the around-timeout portion of
 *         a timer timeout callback.  Note that each class may have
 *         only one around-timeout method and that the method may not
 *         be overloaded.
 *         If the class element is missing then
 *         the class defining the callback is assumed to be the
 *         interceptor class or component class in scope at the
 *         location in the descriptor in which the around-timeout
 *         definition appears.
 * </pre>
 */
public interface AroundTimeout extends CommonDomModelElement {

	/**
	 * Returns the value of the class child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.SubTag ("class")
	GenericDomValue<PsiClass> getClazz();


	/**
	 * Returns the value of the method-name child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:java-identifierType documentation</h3>
	 * The java-identifierType defines a Java identifier.
	 * 	The users of this type should further verify that
	 * 	the content does not contain Java reserved keywords.
	 * </pre>
	 * @return the value of the method-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getMethodName();


}
