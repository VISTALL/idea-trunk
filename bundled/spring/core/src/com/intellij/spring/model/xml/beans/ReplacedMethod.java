// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiMethod;
import com.intellij.spring.model.converters.ReplacedMethodBeanConverter;
import com.intellij.spring.model.converters.SpringBeanReplacedMethodConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/beans:replaced-methodElemType interface.
 */
public interface ReplacedMethod extends DomElement {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * 	The name of the method whose implementation must be replaced by the
   * 	IoC container. If this method is not overloaded, there is no need
   * 	to use arg-type subelements. If this method is overloaded, arg-type
   * 	subelements must be used for all override definitions for the method.
   * <p/>
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  @Convert(value = SpringBeanReplacedMethodConverter.class)
  GenericAttributeValue<PsiMethod> getName();


  /**
   * Returns the value of the replacer child.
   * <pre>
   * <h3>Attribute null:replacer documentation</h3>
   * 	Bean name of an implementation of the MethodReplacer interface in the
   * 	current or ancestor factories. This may be a singleton or prototype
   * 	bean. If it is a prototype, a new instance will be used for each
   * 	method replacement. Singleton usage is the norm.
   * <p/>
   * </pre>
   *
   * @return the value of the replacer child.
   */
  @NotNull
  @Convert(value = ReplacedMethodBeanConverter.class)
  GenericAttributeValue<SpringBeanPointer> getReplacer();


  /**
   * Returns the list of arg-type children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:arg-type documentation</h3>
   * 	Identifies an argument for a replaced method in the event of
   * 	method overloading.
   * <p/>
   * </pre>
         *
         * @return the list of arg-type children.
         */
	@NotNull
	List<ArgType> getArgTypes();
	/**
         * Adds new child to the list of arg-type children.
         *
         * @return created child
         */
	ArgType addArgType();


}
