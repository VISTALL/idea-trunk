package com.intellij.spring.model;

import com.intellij.psi.PsiClass;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.model.xml.CommonSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class SpringFactoryBeansEffectiveTypesProviders extends SpringBeanEffectiveTypeProvider {

  public void processEffectiveTypes(@NotNull final CommonSpringBean bean, final Collection<PsiClass> result) {
    final PsiClass beanClass = bean.getBeanClass();
    if (beanClass == null || !SpringFactoryBeansManager.isBeanFactory(beanClass)) return;

    result.clear();

    result.addAll(Arrays.asList(SpringFactoryBeansManager.getInstance().getProductTypes(beanClass, bean)));
  }
}
