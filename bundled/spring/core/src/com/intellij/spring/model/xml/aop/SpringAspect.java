// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.AopAspect;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/aop:aspectType interface.
 */
public interface SpringAspect extends SpringAopElement, AopAspect {

	/**
	 * Returns the value of the ref child.
	 * <pre>
	 * <h3>Attribute null:ref documentation</h3>
	 * 	The name of the (backing) bean that encapsulates the aspect.
	 * 				
	 * </pre>
	 * @return the value of the ref child.
	 */
	@NotNull
        @Convert(value = SpringBeanResolveConverter.class)
        GenericAttributeValue<SpringBeanPointer> getRef();


	/**
	 * Returns the value of the pointcut child.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:pointcut documentation</h3>
	 * 	A named pointcut definition.
	 * 					
	 * </pre>
	 * @return the value of the pointcut child.
	 */
	@NotNull
        SpringPointcut getPointcut();


	/**
	 * Returns the list of declare-parents children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:declare-parents documentation</h3>
	 * 	Allows this aspect to introduce additional interfaces that the advised
	 * 	object will transparently implement.
	 * 					
	 * </pre>
	 * @return the list of declare-parents children.
	 */
	@NotNull
        @SubTagList("declare-parents")
        List<DeclareParents> getIntroductions();
	/**
	 * Adds new child to the list of declare-parents children.
	 * @return created child
	 */
        @SubTagList("declare-parents")
        DeclareParents addIntroduction();


	/**
	 * Returns the list of before children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:before documentation</h3>
	 * 	A before advice definition.
	 * 						
	 * </pre>
	 * @return the list of before children.
	 */
	@NotNull
	List<BasicAdvice> getBefores();
	/**
	 * Adds new child to the list of before children.
	 * @return created child
	 */
	BasicAdvice addBefore();


	/**
	 * Returns the list of after children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:after documentation</h3>
	 * 	An after advice definition.
	 * 						
	 * </pre>
	 * @return the list of after children.
	 */
	@NotNull
	List<BasicAdvice> getAfters();
	/**
	 * Adds new child to the list of after children.
	 * @return created child
	 */
	BasicAdvice addAfter();


	/**
	 * Returns the list of after-returning children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:after-returning documentation</h3>
	 * 	An after-returning advice definition.
	 * 						
	 * </pre>
	 * @return the list of after-returning children.
	 */
	@NotNull
	List<AfterReturningAdvice> getAfterReturnings();
	/**
	 * Adds new child to the list of after-returning children.
	 * @return created child
	 */
	AfterReturningAdvice addAfterReturning();


	/**
	 * Returns the list of after-throwing children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:after-throwing documentation</h3>
	 * 	An after-throwing advice definition.
	 * 						
	 * </pre>
	 * @return the list of after-throwing children.
	 */
	@NotNull
	List<AfterThrowingAdvice> getAfterThrowings();
	/**
	 * Adds new child to the list of after-throwing children.
	 * @return created child
	 */
	AfterThrowingAdvice addAfterThrowing();


	/**
	 * Returns the list of around children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:around documentation</h3>
	 * 	An around advice definition.
	 * 						
	 * </pre>
	 * @return the list of around children.
	 */
	@NotNull
	List<BasicAdvice> getArounds();
	/**
	 * Adds new child to the list of around children.
	 * @return created child
	 */
	BasicAdvice addAround();

  @SubTagsList({"before", "after", "after-returning", "after-throwing", "around"})
  List<BasicAdvice> getAdvices();


}
