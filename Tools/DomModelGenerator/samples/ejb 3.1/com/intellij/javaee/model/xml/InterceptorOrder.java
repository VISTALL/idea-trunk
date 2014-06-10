// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:interceptor-orderType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:interceptor-orderType documentation</h3>
 * The interceptor-orderType element describes a total ordering
 *         of interceptor classes.
 * </pre>
 */
public interface InterceptorOrder extends CommonDomModelElement {

	/**
	 * Returns the list of interceptor-class children.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the list of interceptor-class children.
	 */
	@NotNull
	@Required
	List<GenericDomValue<PsiClass>> getInterceptorClasses();
	/**
	 * Adds new child to the list of interceptor-class children.
	 * @return created child
	 */
	GenericDomValue<PsiClass> addInterceptorClass();


}
