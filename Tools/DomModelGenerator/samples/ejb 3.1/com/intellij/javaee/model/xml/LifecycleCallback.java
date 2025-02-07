// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:lifecycle-callbackType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:lifecycle-callbackType documentation</h3>
 * The lifecycle-callback type specifies a method on a
 * 	class to be called when a lifecycle event occurs.
 * 	Note that each class may have only one lifecycle callback
 *         method for any given event and that the method may not
 * 	be overloaded.
 *         If the lifefycle-callback-class element is missing then
 *         the class defining the callback is assumed to be the
 *         component class in scope at the place in the descriptor
 *         in which the callback definition appears.
 * </pre>
 */
public interface LifecycleCallback extends CommonDomModelElement {

	/**
	 * Returns the value of the lifecycle-callback-class child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the lifecycle-callback-class child.
	 */
	@NotNull
	GenericDomValue<PsiClass> getLifecycleCallbackClass();


	/**
	 * Returns the value of the lifecycle-callback-method child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:java-identifierType documentation</h3>
	 * The java-identifierType defines a Java identifier.
	 * 	The users of this type should further verify that
	 * 	the content does not contain Java reserved keywords.
	 * </pre>
	 * @return the value of the lifecycle-callback-method child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getLifecycleCallbackMethod();


}
