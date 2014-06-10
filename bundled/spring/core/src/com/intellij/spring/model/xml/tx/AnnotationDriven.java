// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tx

package com.intellij.spring.model.xml.tx;

import com.intellij.aop.AopAspect;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.SpringAopAdvice;
import com.intellij.spring.model.xml.beans.TypedBeanPointerAttribute;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/tx:annotation-drivenElemType interface.
 * <pre>
 * <h3>Type http://www.springframework.org/schema/tx:annotation-drivenElemType documentation</h3>
 * 	Indicates that transaction configuration is defined by Java5
 * 	annotations on bean classes, and that proxies are automatically
 * 	to be created for the relevant annotated beans.
 * 	
 * 	Transaction semantics such as propagation settings, the isolation
 * 	level, the rollback rules, etc. are all defined in the annotation                       
 * 	metadata.
 * 				
 * </pre>
 */
public interface AnnotationDriven extends SpringTxElement, DomSpringBean, SpringAopAdvice, AopAspect {

	/**
	 * Returns the value of the transaction-manager child.
	 * <pre>
	 * <h3>Attribute null:transaction-manager documentation</h3>
	 * 	The bean name of the PlatformTransactionManager that is to be used
	 * 	to drive transactions.
	 * 	
	 * 	This attribute is not required, and only needs to be specified
	 * 	explicitly if the bean name of the desired PlatformTransactionManager
	 * 	is not 'transactionManager'.
	 * 					
	 * </pre>
	 * @return the value of the transaction-manager child.
	 */
  @RequiredBeanType("org.springframework.transaction.PlatformTransactionManager")
  TypedBeanPointerAttribute getTransactionManager();

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


}
