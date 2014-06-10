package com.intellij.webBeans.beans;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public abstract class AbstractWebBeanDescriptor<T extends PsiMember> implements  WebBeanDescriptor {

  @NotNull
  public abstract T getAnnotatedItem();

  @Nullable
  protected abstract PsiClass getDefaultDeploymentType();

  public Set<PsiClass> getBindingTypes() {
    Set<PsiClass> bindingTypes = new HashSet<PsiClass>();
    Collection<String> qualifiedNames = WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getBindingTypesClasses(getModule()));

    PsiAnnotation[] annotations = AnnotationUtil.findAnnotations(getAnnotatedItem(), qualifiedNames);

    if (annotations.length >0) {
      bindingTypes.addAll(ContainerUtil.map2Set(annotations, new Function<PsiAnnotation, PsiClass>() {
        public PsiClass fun(PsiAnnotation psiAnnotation) {
          return getAnnotationClass(psiAnnotation.getQualifiedName());
        }
      }));
    }
    if (AnnotationUtil.isAnnotated(getAnnotatedItem(), WebBeansAnnoConstants.SPECIALIZES_ANNOTATION, true)) {
    // todo add specialized binding types if @Specializes
    } else {
      PsiClass currentAnnotationiClass = getAnnotationClass(WebBeansAnnoConstants.CURRENT_ANNOTATION);
      if (currentAnnotationiClass != null) {
        bindingTypes.add(currentAnnotationiClass);
      }
    }

    return bindingTypes;
  }

  @Nullable
  public PsiClass getScopeType() {
    //todo !!! analyse @Stereotype
    Collection<String> qualifiedNames = WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getScopeTypesClasses(getModule()));

    PsiAnnotation[] annotations = AnnotationUtil.findAnnotations(getAnnotatedItem(), qualifiedNames);

    if (annotations.length > 0) return getAnnotationClass(annotations[0].getQualifiedName());

    return getAnnotationClass(WebBeansAnnoConstants.DEPENDENT_ANNOTATION);
  }

  @Nullable
  public PsiClass getDeploymentType() {
    //todo !!! analyse @Stereotype
    Collection<String> qualifiedNames = WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getDeploymentTypesClasses(getModule()));

    PsiAnnotation[] annotations = AnnotationUtil.findAnnotations(getAnnotatedItem(), qualifiedNames);

    if (annotations.length > 0) return getAnnotationClass(annotations[0].getQualifiedName());

    return getDefaultDeploymentType();
  }


  @NotNull
  public Set<PsiClass> getStereotypes() {
    Set<PsiClass> stereotypes = new HashSet<PsiClass>();
    Collection<PsiClass> stereotypeAnnotationClasses = WebBeansCommonUtils.getStereotypeAnnotationClasses(getModule());

    T annotatedItem = getAnnotatedItem();
    for (PsiClass psiClass : stereotypeAnnotationClasses) {
      if (AnnotationUtil.isAnnotated(annotatedItem, psiClass.getQualifiedName(), true)) stereotypes.add(psiClass);
    }

    return stereotypes;
  }

  @Nullable
  protected PsiClass getAnnotationClass(@NotNull String annotation) {
    JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(getModule().getProject());

    return psiFacade.findClass(annotation, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(getModule()));
  }

  @NotNull
  protected Module getModule() {
    Module module = ModuleUtil.findModuleForPsiElement(getAnnotatedItem());

    assert module != null;

    return module;
  }
}
