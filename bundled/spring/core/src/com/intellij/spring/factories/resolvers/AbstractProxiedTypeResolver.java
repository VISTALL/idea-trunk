package com.intellij.spring.factories.resolvers;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Taras Tielkes
 */
public abstract class AbstractProxiedTypeResolver extends AbstractTypeResolver {

  @NonNls protected static final String PROXY_CLASS_FLAG_PROPERTY_NAME = "proxyTargetClass";
  @NonNls protected static final String OPTIMIZE_PROPERTY_NAME = "optimize";

  // @see org.springframework.util.ClassUtils#getAllInterfacesForClass(Class clazz)
  @NotNull
  protected static Set<PsiClass> getAllInterfaces(@NotNull final PsiClassType classType) {

    PsiClass psiClass = classType.resolve();
    if (psiClass != null) {

      if (psiClass.isInterface()) {
        return Collections.singleton(psiClass);
      }

      final Set<PsiClass> interfaces = new HashSet<PsiClass>();
      while (psiClass != null) {
        interfaces.addAll(Arrays.asList(psiClass.getInterfaces()));
        psiClass = psiClass.getSuperClass();
      }
      return interfaces;
    }
    return Collections.emptySet();
  }

  @NotNull
  protected static Set<String> getAllInterfaceNames(@NotNull final PsiClassType type) {
    final Set<PsiClass> interfaces = getAllInterfaces(type);
    final Set<String> names = new HashSet<String>(interfaces.size());
    for (PsiClass anInterface : interfaces) {
      names.add(anInterface.getQualifiedName());
    }
    return names;
  }

  protected static boolean isCglibExplicitlyEnabled(@NotNull final SpringBean context) {
    return isBooleanProperySetAndTrue(context, PROXY_CLASS_FLAG_PROPERTY_NAME) ||
        isBooleanProperySetAndTrue(context, OPTIMIZE_PROPERTY_NAME);
  }
}
