// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tool

package com.intellij.spring.model.xml.tool;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/tool:exportsType interface.
 * <pre>
 * <h3>Type http://www.springframework.org/schema/tool:exportsType documentation</h3>
 * Indicates that an annotated types exports an application visible component.
 * </pre>
 */
public interface Exports extends SpringToolElement {

	/**
	 * Returns the value of the type child.
	 * <pre>
	 * <h3>Attribute null:type documentation</h3>
	 * The type of the exported component. May be null if the type is not known until runtime.
	 * </pre>
	 * @return the value of the type child.
	 */
	@NotNull
	GenericAttributeValue<String> getType();


	/**
	 * Returns the value of the identifier child.
	 * <pre>
	 * <h3>Attribute null:identifier documentation</h3>
	 * Defines an XPath query that can be executed against the node annotated with this
	 * 					type to determine the identifier of any exported component.
	 * </pre>
	 * @return the value of the identifier child.
	 */
	@NotNull
	GenericAttributeValue<String> getIdentifier();


}
