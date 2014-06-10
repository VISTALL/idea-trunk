package com.intellij.spring.factories.resolvers;

import com.intellij.psi.PsiClassType;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Resolves factory product type for a ScopedProxyFactoryBean.
 *
 * @author Taras Tielkes
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class ScopedProxyFactoryBeanTypeResolver extends AbstractProxiedTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.aop.scope.ScopedProxyFactoryBean";

  @NonNls private static final String TARGET_BEAN_NAME_PROPERTY_NAME = "targetBeanName";

  @NotNull
  public Set<String> getObjectType(@NotNull CommonSpringBean context) {
    if (context instanceof SpringBean) {
      final SpringBean springBean = (SpringBean) context;
      final PsiClassType type = getTargetType(springBean);

      if (type != null) {
        if (isBooleanProperySetAndFalse(springBean, PROXY_CLASS_FLAG_PROPERTY_NAME)) {
          final Set<String> targetInterfaceNames = getAllInterfaceNames(type);
          if (!targetInterfaceNames.isEmpty()) {
            return targetInterfaceNames;
          }
        }
        return Collections.singleton(type.getCanonicalText());
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  private static PsiClassType getTargetType(@NotNull SpringBean context) {
    final String targetBeanName = getPropertyValue(context, TARGET_BEAN_NAME_PROPERTY_NAME);
    if (targetBeanName != null) {
      final PsiClassType fromTargetName = getTypeFromBeanName(context, targetBeanName);
      if (fromTargetName != null) {
        return fromTargetName;
      }
    }
    return null;
  }

  public boolean accept(@NotNull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
