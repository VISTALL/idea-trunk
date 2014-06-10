// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/aop:pointcutType interface.
 */
public interface SpringPointcut extends SpringAopElement, AopPointcut {

	/**
	 * Returns the value of the expression child.
	 * <pre>
	 * <h3>Attribute null:expression documentation</h3>
	 * 	The pointcut expression.
	 * 	
	 * 	For example : 'execution(* com.xyz.myapp.service.*.*(..))'
	 * 				
	 * </pre>
	 * @return the value of the expression child.
	 */

        @NameValue
        GenericAttributeValue<String> getId();

        @NotNull
	@Required
        @Convert(PointcutExpressionConverter.class)
        GenericAttributeValue<PsiPointcutExpression> getExpression();


	/**
	 * Returns the value of the type child.
	 * <pre>
	 * <h3>Attribute null:type documentation</h3>
	 * 	The type of pointcut expression (see the associated enumeration).
	 * 				
	 * </pre>
	 * @return the value of the type child.
	 */
	@NotNull
	GenericAttributeValue<PointcutType> getType();

  @PropertyAccessor("id")
  GenericValue<String> getQualifiedName();
}
