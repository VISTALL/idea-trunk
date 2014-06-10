package com.intellij.spring.model;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.context.AnnotationConfig;
import com.intellij.spring.model.xml.context.ComponentScan;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class SpringAnnotationConfigUtils {

  @NonNls public static String AUTOWIRED_ANNOTATION_BPP =
      "org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor"; // BPP - BeanPostProcessor :)
  @NonNls public static String REQUIRED_ANNOTATION_BPP = "org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor";
  @NonNls public static String COMMON_ANNOTATION_BPP = "org.springframework.context.annotation.CommonAnnotationBeanPostProcessor";
  @NonNls public static String PERSISTENCE_ANNOTATION_BPP = "org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor";

  private SpringAnnotationConfigUtils() {
  }

  // <context:annotation-config/> : implicitly registered post-processors include AutowiredAnnotationBeanPostProcessor, CommonAnnotationBeanPostProcessor, PersistenceAnnotationBeanPostProcessor, RequiredAnnotationBeanPostProcessor.
  public static boolean containsAnnotationConfigBean(@NotNull final SpringModel springModel) {
    for (SpringBaseBeanPointer beanPointer : springModel.getAllDomBeans()) {
      final CommonSpringBean domSpringBean = beanPointer.getSpringBean();
      if (domSpringBean instanceof AnnotationConfig) {
        return true;
      }
    }
    return false;
  }

  // <context:annotation-config/> : implicitly registered post-processors include AutowiredAnnotationBeanPostProcessor, CommonAnnotationBeanPostProcessor, PersistenceAnnotationBeanPostProcessor, RequiredAnnotationBeanPostProcessor.
  public static boolean containsComponentScanBean(@NotNull final SpringModel springModel) {
    for (SpringBaseBeanPointer beanPointer : springModel.getAllDomBeans()) {
      final CommonSpringBean domSpringBean = beanPointer.getSpringBean();
      if (domSpringBean instanceof ComponentScan) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsAutowiredAnnotationBeanPostProcessor(@NotNull final SpringModel springModel) {
    return isBeanExists(springModel, AUTOWIRED_ANNOTATION_BPP) || containsAnnotationConfigBean(springModel) || containsComponentScanBean(springModel);
  }

  public static boolean containsRequiredAnnotationBeanPostProcessor(@NotNull final SpringModel springModel) {
    return isBeanExists(springModel, REQUIRED_ANNOTATION_BPP) || containsAnnotationConfigBean(springModel);
  }

  public static boolean containsCommonAnnotationBeanPostProcessor(@NotNull final SpringModel springModel) {
    return isBeanExists(springModel, COMMON_ANNOTATION_BPP) || containsAnnotationConfigBean(springModel) || containsComponentScanBean(springModel);
  }

  public static boolean containsPersistenceAnnotationBeanPostProcessor(@NotNull final SpringModel springModel) {
    return isBeanExists(springModel, PERSISTENCE_ANNOTATION_BPP) || containsAnnotationConfigBean(springModel);
  }

  private static boolean isBeanExists(final SpringModel springModel, final String qualifiedName) {
    final Module module = springModel.getModule();
    if (module != null) {
      final PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject())
          .findClass(qualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
      if (psiClass != null) {
        return springModel.findBeansByPsiClass(psiClass).size() > 0;
      }
    }
    return false;
  }
}
