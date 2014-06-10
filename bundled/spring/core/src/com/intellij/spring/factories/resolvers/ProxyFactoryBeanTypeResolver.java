package com.intellij.spring.factories.resolvers;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
/**
 * Resolves factory product type for a ProxyFactoryBean.
 * <p/>
 * The approach mirrors the logic in {@link org.springframework.aop.framework.ProxyFactoryBean} and
 * {@link org.springframework.aop.framework.DefaultAopProxyFactory}:
 * <ol>
 * <li>If "proxyTargetClass" or "optimize" is set to true, resolve to type of target property
 * <li>If "proxyInterfaces" or "interfaces" is set, resolve to a type implementing the specified interfaces
 * <li>If target type is known, and "autodetectInterfaces" is not disabled, resolve to a type implementing
 * all interfaces of target
 * <li>Otherwise, fall back to type of target
 * </ol>
 *
 * @author Taras Tielkes
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class ProxyFactoryBeanTypeResolver extends AbstractProxiedTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.aop.framework.ProxyFactoryBean";

  @NonNls private static final String PROXY_INTERFACES_PROPERTY_NAME = "proxyInterfaces";
  @NonNls private static final String INTERFACES_PROPERTY_NAME = "interfaces";
  @NonNls private static final String AUTODETECT_INTERFACES_PROPERTY_NAME = "autodetectInterfaces";

  @NonNls private static final String TARGET_PROPERTY_NAME = "target";
  @NonNls private static final String TARGET_NAME_PROPERTY_NAME = "targetName";
  @NonNls private static final String TARGET_CLASS_PROPERTY_NAME = "targetClass";

  @NotNull
  public Set<String> getObjectType(@NotNull CommonSpringBean context) {
    if (context instanceof SpringBean) {
      final SpringBean springBean = (SpringBean) context;
      final PsiClassType type = getTargetType(springBean);

      if (isCglibExplicitlyEnabled(springBean) && type != null) {
        return Collections.singleton(type.getCanonicalText());
      }

      final Set<String> proxyInterfaceNames = getTypesFromClassArrayProperty(springBean, PROXY_INTERFACES_PROPERTY_NAME);
      if (!proxyInterfaceNames.isEmpty()) {
        return proxyInterfaceNames;
      }

      final Set<String> interfaceNames = getTypesFromClassArrayProperty(springBean, INTERFACES_PROPERTY_NAME);
      if (!interfaceNames.isEmpty()) {
        return interfaceNames;
      }

      if (type != null) {
        final Set<String> targetInterfaceNames = getAllInterfaceNames(type);
        if (!targetInterfaceNames.isEmpty() && isAutodetectInterfacesEnabled(springBean)) {
          return targetInterfaceNames;
        } else {
          return Collections.singleton(type.getCanonicalText());
        }
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  private static PsiClassType getTargetType(@NotNull SpringBean context) {
    final PsiClassType fromTarget = getTypeFromProperty(context, TARGET_PROPERTY_NAME);
    if (fromTarget != null) {
      return fromTarget;
    }

    final String targetName = getPropertyValue(context, TARGET_NAME_PROPERTY_NAME);
    if (targetName != null) {
      final PsiClassType fromTargetName = getTypeFromBeanName(context, targetName);
      if (fromTargetName != null) {
        return fromTargetName;
      }
    }

    final String targetClassName = getPropertyValue(context, TARGET_CLASS_PROPERTY_NAME);
    if (targetClassName != null) {
      final Project project = context.getManager().getProject();
      final PsiManager psiManager = PsiManager.getInstance(project);
      final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
      final PsiClass targetClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(targetClassName, scope);
      if (targetClass != null) {
        return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(targetClass);
      }
    }
    return null;
  }

  private static boolean isAutodetectInterfacesEnabled(@NotNull SpringBean context) {
    return !isBooleanProperySetAndFalse(context, AUTODETECT_INTERFACES_PROPERTY_NAME);
  }

  public boolean accept(@NotNull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
