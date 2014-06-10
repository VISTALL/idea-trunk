// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.aop.AopAfterReturningAdvice;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/aop:afterReturningAdviceType interface.
 */
public interface AfterReturningAdvice extends DomElement, BasicAdvice, AopAfterReturningAdvice {

	/**
	 * Returns the value of the returning child.
	 * <pre>
	 * <h3>Attribute null:returning documentation</h3>
	 * 	The name of the method parameter to which the return value must
	 * 	be passed.
	 * 						
	 * </pre>
	 * @return the value of the returning child.
	 */
	@NotNull
        @Convert(value=AdviceParameterConverter.class, soft=true)
        GenericAttributeValue<PsiParameter> getReturning();


	/**
	 * Returns the value of the pointcut child.
	 * <pre>
	 * <h3>Attribute null:pointcut documentation</h3>
	 * 	The associated pointcut expression.
	 * 				
	 * </pre>
	 * @return the value of the pointcut child.
	 */
	@NotNull
	GenericAttributeValue<PsiPointcutExpression> getPointcut();


	/**
	 * Returns the value of the pointcut-ref child.
	 * <pre>
	 * <h3>Attribute null:pointcut-ref documentation</h3>
	 * 	The name of an associated pointcut definition.
	 * 				
	 * </pre>
	 * @return the value of the pointcut-ref child.
	 */
	@NotNull
	GenericAttributeValue<SpringPointcut> getPointcutRef();


	/**
	 * Returns the value of the method child.
	 * <pre>
	 * <h3>Attribute null:method documentation</h3>
	 * 	The name of the method that defines the logic of the advice.
	 * 				
	 * </pre>
	 * @return the value of the method child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<PsiMethod> getMethod();


	/**
	 * Returns the value of the arg-names child.
	 * <pre>
	 * <h3>Attribute null:arg-names documentation</h3>
	 * 	The comma-delimited list of advice method argument (parameter) names 
	 * 	that will be matched from pointcut parameters.
	 * 				
	 * </pre>
	 * @return the value of the arg-names child.
	 */
	@NotNull
	GenericAttributeValue<String> getArgNames();


}
