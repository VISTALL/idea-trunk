package com.intellij.spring.model.xml;

import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.NameValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommonSpringBean extends CommonModelElement {
  @Nullable
  @NameValue    
  String getBeanName();

  /**
   * Return aliases defined in the bean definition, not including the bean name.
   * @return bean aliases
   * @see com.intellij.spring.SpringModel#getAllBeanNames(String)
   */
  @NotNull
  String[] getAliases();

  @Nullable
  PsiClass getBeanClass(boolean considerFactories);

  @Nullable
  PsiClass getBeanClass();

  @Nullable
  SpringQualifier getSpringQualifier();
}
