// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:post-remove interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:post-remove documentation</h3>
 * @Target({METHOD}) @Retention(RUNTIME)
 *         public @interface PostRemove {}
 * </pre>
 */
public interface PostRemove extends JavaeeDomModelElement {

	/**
	 * Returns the value of the method-name child.
	 * @return the value of the method-name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getMethodName();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


}
