// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:method-paramsType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:method-paramsType documentation</h3>
 * The method-paramsType defines a list of the
 * 	fully-qualified Java type names of the method parameters.
 * </pre>
 */
public interface MethodParams extends CommonDomModelElement {

	/**
	 * Returns the list of method-param children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:method-param documentation</h3>
	 * The method-param element contains a primitive
	 * 	    or a fully-qualified Java type name of a method
	 * 	    parameter.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:java-typeType documentation</h3>
	 * This is a generic type that designates a Java primitive
	 * 	type or a fully qualified name of a Java interface/type,
	 * 	or an array of such types.
	 * </pre>
	 * @return the list of method-param children.
	 */
	@NotNull
	List<GenericDomValue<String>> getMethodParams();
	/**
	 * Adds new child to the list of method-param children.
	 * @return created child
	 */
	GenericDomValue<String> addMethodParam();


}
