// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.NamedMethod;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:init-methodType interface.
 */
public interface InitMethod extends CommonDomModelElement {

	/**
	 * Returns the value of the create-method child.
	 * @return the value of the create-method child.
	 */
	@NotNull
	@Required
	NamedMethod getCreateMethod();


	/**
	 * Returns the value of the bean-method child.
	 * @return the value of the bean-method child.
	 */
	@NotNull
	@Required
	NamedMethod getBeanMethod();


}
