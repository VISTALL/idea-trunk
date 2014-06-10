// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:application-exceptionType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:application-exceptionType documentation</h3>
 * The application-exceptionType declares an application
 *         exception. The declaration consists of:
 *             - the exception class. When the container receives
 *               an exception of this type, it is required to
 *               forward this exception as an applcation exception
 *               to the client regardless of whether it is a checked
 *               or unchecked exception.
 *             - an optional rollback element. If this element is
 *               set to true, the container must rollback the current
 *               transaction before forwarding the exception to the
 *               client.  If not specified, it defaults to false.
 *             - an optional inherited element. If this element is
 *               set to true, subclasses of the exception class type
 * 	      are also automatically considered application
 * 	      exceptions (unless overriden at a lower level).
 * 	      If set to false, only the exception class type is
 * 	      considered an application-exception, not its
 * 	      exception subclasses. If not specified, this
 *  	      value defaults to true.
 * </pre>
 */
public interface ApplicationException extends CommonDomModelElement {

	/**
	 * Returns the value of the exception-class child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the exception-class child.
	 */
	@NotNull
	@Required
	GenericDomValue<PsiClass> getExceptionClass();


	/**
	 * Returns the value of the rollback child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the rollback child.
	 */
	@NotNull
	GenericDomValue<Boolean> getRollback();


	/**
	 * Returns the value of the inherited child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the inherited child.
	 */
	@NotNull
	GenericDomValue<Boolean> getInherited();


}
