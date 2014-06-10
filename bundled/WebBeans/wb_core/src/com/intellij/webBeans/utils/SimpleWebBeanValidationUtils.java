package com.intellij.webBeans.utils;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.javaee.ejb.EjbHelper;
import com.intellij.javaee.model.common.persistence.JpaAnnotationConstants;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class SimpleWebBeanValidationUtils {

  // spec 3.2.1
  private SimpleWebBeanValidationUtils() {

  }

  private static final String[] unallowedAncestors =
    new String[]{"javax.servlet.Servlet", "javax.servlet.Filter", "javax.servlet.ServletContextListener",
      "javax.servlet.HttpSessionListener", "javax.servlet.ServletRequestListener", "javax.ejb.EnterpriseBean",
      "javax.faces.component.UIComponent"};

  public static boolean isSimpleWebBean(PsiClass psiClass) {
    return !isParameterizedType(psiClass) &&
           (isConcreteClass(psiClass) || isDecoratorClass(psiClass)) &&
           !isNonStaticInner(psiClass) &&
           hasAppropriateConstructor(psiClass) &&
           !hasUnallowedAncestor(psiClass) &&
           !isEjbBean(psiClass) &&
           !isJpaEntity(psiClass);
  }

  public static boolean isJpaEntity(PsiClass psiClass) {
    return AnnotationUtil.isAnnotated(psiClass, JpaAnnotationConstants.ENTITY_ANNO, false);
  }

  public static boolean isEjbBean(PsiClass psiClass) {
    return EjbHelper.getEjbHelper().getEjbRoles(psiClass).length > 0;
  }

  public static boolean hasAppropriateConstructor(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.getConstructors();

    if (methods.length == 0) return true;

    for (PsiMethod psiMethod : methods) {
      if (psiMethod.getParameterList().getParametersCount() == 0 ||
          AnnotationUtil.isAnnotated(psiMethod, WebBeansAnnoConstants.INITIALIZER_ANNOTATION, true)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasUnallowedAncestor(PsiClass psiClass) {
    if (getUnallowedAncestor(psiClass) != null) return true;
    return false;
  }

  @Nullable
  public static String getUnallowedAncestor(PsiClass psiClass) {
    JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(psiClass.getProject());
    for (String ancestorFQN : unallowedAncestors) {
      PsiClass ancestorClass = psiFacade.findClass(ancestorFQN, GlobalSearchScope.allScope(psiClass.getProject()));

      if (ancestorClass != null && psiClass.isInheritor(ancestorClass, true)) return ancestorFQN;
    }
    return null;
  }

  public static boolean isNonStaticInner(PsiClass psiClass) {
    return !psiClass.hasModifierProperty(PsiModifier.STATIC) && psiClass.getContainingClass() != null;
  }

  public static boolean isConcreteClass(PsiClass psiClass) {
    return !psiClass.isInterface() && !psiClass.isEnum() && !psiClass.isAnnotationType();
  }

  public static boolean isDecoratorClass(PsiClass psiClass) {
    return AnnotationUtil.isAnnotated(psiClass, WebBeansAnnoConstants.DECORATOR_ANNOTATION, false);
  }

  public static boolean isParameterizedType(PsiClass psiClass) {
    return psiClass.hasTypeParameters();
  }
}
