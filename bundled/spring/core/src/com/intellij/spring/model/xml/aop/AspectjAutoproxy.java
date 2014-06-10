// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.spring.model.xml.DomSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/aop:aspectj-autoproxyElemType interface.
 */
public interface AspectjAutoproxy extends SpringAopElement, DomSpringBean {

	/**
	 * Returns the value of the proxy-target-class child.
	 * <pre>
	 * <h3>Attribute null:proxy-target-class documentation</h3>
	 * 	Are class-based (CGLIB) proxies to be created? By default, standard
	 * 	Java interface-based proxies are created.
	 * 					
	 * </pre>
	 * @return the value of the proxy-target-class child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getProxyTargetClass();


	/**
	 * Returns the list of include children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:include documentation</h3>
	 * 	Indicates that only @AspectJ beans with names matched by the pattern
	 * 	will be considered as defining aspects to use for Spring autoproxying.
	 * 						
	 * </pre>
	 * @return the list of include children.
	 */
	@NotNull
	List<AopInclude> getIncludes();
	/**
	 * Adds new child to the list of include children.
	 * @return created child
	 */
	AopInclude addInclude();


}
