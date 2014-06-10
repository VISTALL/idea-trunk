// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.model.xml.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.psi.AopReferenceHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.ClassKind;
import com.intellij.util.xml.ClassTemplate;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/aop:declareParentsType interface.
 */
public interface DeclareParents extends SpringAopElement, AopIntroduction {

	/**
	 * Returns the value of the types-matching child.
	 * <pre>
	 * <h3>Attribute null:types-matching documentation</h3>
	 * 	The AspectJ type expression that defines what types (classes) the
	 * 	introduction is restricted to.
	 * 	 
	 * 	An example would be 'org.springframework.beans.ITestBean+'.
	 * 				
	 * </pre>
	 * @return the value of the types-matching child.
	 */
	@NotNull
	@Required
        @Convert(com.intellij.spring.model.xml.aop.TypesMatchingConverter.class)
        GenericAttributeValue<AopReferenceHolder> getTypesMatching();


	/**
	 * Returns the value of the implement-interface child.
	 * <pre>
	 * <h3>Attribute null:implement-interface documentation</h3>
	 * 	The fully qualified name of the interface that will be introduced. 
	 * 				
	 * </pre>
	 * @return the value of the implement-interface child.
	 */
	@NotNull
	@Required
        @ClassTemplate(kind= ClassKind.INTERFACE)  
	GenericAttributeValue<PsiClass> getImplementInterface();


	/**
	 * Returns the value of the default-impl child.
	 * <pre>
	 * <h3>Attribute null:default-impl documentation</h3>
	 * 	The fully qualified name of the class that will be instantiated to serve
	 * 	as the default implementation of the introduced interface. 
	 * 				
	 * </pre>
	 * @return the value of the default-impl child.
	 */
	@NotNull
	GenericAttributeValue<PsiClass> getDefaultImpl();


}
