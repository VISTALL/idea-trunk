// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:baseCollectionType interface.
 */
public interface BaseCollection extends DomElement {

	/**
	 * Returns the value of the merge child.
	 * <pre>
	 * <h3>Attribute null:merge documentation</h3>
	 * 	Enables/disables merging for collections when using parent/child beans.
	 * 				
	 * </pre>
	 * @return the value of the merge child.
	 */
	@NotNull
	GenericAttributeValue<DefaultableBoolean> getMerge();


}
