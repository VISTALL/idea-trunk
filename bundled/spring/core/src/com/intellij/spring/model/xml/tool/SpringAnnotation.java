// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tool

package com.intellij.spring.model.xml.tool;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/tool:annotationElemType interface.
 */
public interface SpringAnnotation extends SpringToolElement {

	/**
	 * Returns the value of the kind child.
	 * @return the value of the kind child.
	 */
	@NotNull
	GenericAttributeValue<SpringAnnotationKind> getKind();


	/**
	 * Returns the value of the expected-type child.
	 * @return the value of the expected-type child.
	 */
	@NotNull
	TypedParameter getExpectedType();


	/**
	 * Returns the value of the assignable-to child.
	 * @return the value of the assignable-to child.
	 */
	@NotNull
	TypedParameter getAssignableTo();


	/**
	 * Returns the value of the exports child.
	 * @return the value of the exports child.
	 */
	@NotNull
	Exports getExports();


}
