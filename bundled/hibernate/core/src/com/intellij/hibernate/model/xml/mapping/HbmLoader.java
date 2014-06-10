// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-mapping-3.0.dtd:loader interface.
 * Type loader documentation
 * <pre>
 *  The loader element allows specification of a named query to be used for fetching
 * an entity or collection 
 * </pre>
 */
public interface HbmLoader extends CommonDomModelElement {

	/**
	 * Returns the value of the query-ref child.
	 * Attribute query-ref
	 * @return the value of the query-ref child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getQueryRef();


}
