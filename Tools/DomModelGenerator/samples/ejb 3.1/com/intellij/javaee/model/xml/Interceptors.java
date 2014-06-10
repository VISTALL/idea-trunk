// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.Interceptor;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:interceptorsType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:interceptorsType documentation</h3>
 * The interceptorsType element declares one or more interceptor
 *         classes used by components within this ejb-jar.  The declaration
 *         consists of :
 *             - An optional description.
 *             - One or more interceptor elements.
 * </pre>
 */
public interface Interceptors extends CommonDomModelElement {

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
	 * Returns the list of interceptor children.
	 * @return the list of interceptor children.
	 */
	@NotNull
	@Required
	List<Interceptor> getInterceptors();
	/**
	 * Adds new child to the list of interceptor children.
	 * @return created child
	 */
	Interceptor addInterceptor();


}
