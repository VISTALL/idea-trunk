// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tx

package com.intellij.spring.model.xml.tx;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.beans.Identified;
import com.intellij.spring.model.xml.beans.TypedBeanPointerAttribute;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/tx:adviceElemType interface.
 * <pre>
 * <h3>Type http://www.springframework.org/schema/tx:adviceElemType documentation</h3>
 * 	Defines the transactional semantics of the AOP advice that is to be
 * 	executed.
 * 	
 * 	That is, this advice element is where the transactional semantics of
 * 	any	number of methods are defined (where transactional semantics
 * 	includes the propagation settings, the isolation level, the rollback
 * 	rules, etc.).
 * 				
 * </pre>
 */
public interface Advice extends SpringTxElement, Identified, DomSpringBean {

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
  @NotNull
  @RequiredBeanType("org.springframework.transaction.PlatformTransactionManager")
  TypedBeanPointerAttribute getTransactionManager();

	/**
	 * Returns the value of the attributes child.
	 * @return the value of the attributes child.
	 */
	@NotNull
	Attributes getAttributes();


}
