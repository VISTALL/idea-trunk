// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiType;
import com.intellij.spring.model.converters.ConstructorArgIndexConverter;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Namespace;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:constructor-argElemType interface.
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface ConstructorArg extends SpringInjection {

  boolean isAssignable(@NotNull PsiType to);

  /**
   * Returns the value of the index child.
   * <pre>
   * <h3>Attribute null:index documentation</h3>
   * 	The exact index of thr argument in the constructor argument list.
   * 	Only needed to avoid ambiguities, e.g. in case of 2 arguments of
   * 	the exact same type.
   *
   * </pre>
   * @return the value of the index child.
   */
  @Referencing(ConstructorArgIndexConverter.class)
  @NotNull
  GenericAttributeValue<Integer> getIndex();

  /**
   * Returns the value of the type child.
   * <pre>
   * <h3>Attribute null:type documentation</h3>
   * 	The exact type of the constructor argument. Only needed to avoid
   * 	ambiguities, e.g. in case of 2 single argument constructors
   * 	that can both be converted from a String.
   *
   * </pre>
   * @return the value of the type child.
   */
  @NotNull
  GenericAttributeValue<PsiType> getType();
}
