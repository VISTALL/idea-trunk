// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.NamedMethod;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:remove-methodType interface.
 */
public interface RemoveMethod extends CommonDomModelElement {

	/**
	 * Returns the value of the bean-method child.
	 * @return the value of the bean-method child.
	 */
	@NotNull
	@Required
	NamedMethod getBeanMethod();


	/**
	 * Returns the value of the retain-if-exception child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the retain-if-exception child.
	 */
	@NotNull
	GenericDomValue<Boolean> getRetainIfException();


}
