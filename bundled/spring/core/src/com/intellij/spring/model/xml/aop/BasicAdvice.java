// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.model.converters.SpringAdviceMethodConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * http://www.springframework.org/schema/aop:basicAdviceType interface.
 */
public interface BasicAdvice extends SpringAopElement, SpringAopAdvice {

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
        @Convert(PointcutExpressionConverter.class)
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

  @Nullable
  PsiPointcutExpression getPointcutExpression();


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
        @Convert(SpringAdviceMethodConverter.class)
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


  @NotNull
  SpringAdvisedElementsSearcher getSearcher();
}
