package com.intellij.spring.factories.resolvers;

import com.intellij.psi.PsiClassType;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * @author Taras Tielkes
 */
public class BeanReferenceFactoryBeanTypeResolver extends AbstractTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.beans.factory.config.BeanReferenceFactoryBean";
  @NonNls private static final String TARGET_NAME_PROPERTY_NAME = "targetBeanName";

  @NotNull
  public Set<String> getObjectType(@NotNull final CommonSpringBean context) {
    if (context instanceof SpringBean) {
      final SpringBean bean = (SpringBean)context;
      final String targetBeanName = getPropertyValue(context, TARGET_NAME_PROPERTY_NAME);
      if (targetBeanName != null) {
        final PsiClassType fromTargetName = getTypeFromBeanName(bean, targetBeanName);
        if (fromTargetName != null) {
          return Collections.singleton(fromTargetName.getCanonicalText());
        }
      }
    }
    return Collections.emptySet();
  }

  public boolean accept(@NotNull final String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
