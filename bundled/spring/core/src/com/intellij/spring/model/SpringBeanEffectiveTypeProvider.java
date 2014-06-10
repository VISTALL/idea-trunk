package com.intellij.spring.model;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class SpringBeanEffectiveTypeProvider {
  @NonNls public static final ExtensionPointName<SpringBeanEffectiveTypeProvider> BEAN_EFFECTIVE_TYPE_PROVIDER_EXTENSION_POINT = ExtensionPointName.create("com.intellij.spring.effective.types.provider");

  public abstract void processEffectiveTypes(@NotNull final CommonSpringBean bean, Collection<PsiClass> result);

  public boolean createCustomProblem(@NotNull final CommonSpringBean bean, @NotNull PsiType expectedType, final DomElementAnnotationHolder holder,
                                     final DomElement element) {
    return false;
  }

}
