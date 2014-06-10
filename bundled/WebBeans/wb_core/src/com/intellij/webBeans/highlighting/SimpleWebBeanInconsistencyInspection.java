package com.intellij.webBeans.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import static com.intellij.webBeans.utils.SimpleWebBeanValidationUtils.*;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleWebBeanInconsistencyInspection extends BaseWebBeanInspection {

  @Override
  protected void checkClass(PsiClass aClass, ProblemsHolder holder, @NotNull Module module) {
    if (aClass.isAnnotationType() || WebBeansCommonUtils.isSimpleWebBean(aClass) || WebBeansCommonUtils.isEnterpaiseWebBean(aClass)) return;

    if (isClassContainsWebBeansAnnotations(aClass, module)) {
      if (!hasAppropriateConstructor(aClass)) {
        holder.registerProblem(aClass.getNameIdentifier(),  WebBeansInspectionBundle.message("SimpleWebBeanInconsistencyInspection.no.appropriate.constructor"));
      }

      if (!(isConcreteClass(aClass) || isDecoratorClass(aClass))) {
        holder.registerProblem(aClass.getNameIdentifier(),  WebBeansInspectionBundle.message("SimpleWebBeanInconsistencyInspection.not.concrete.class"));
      }

      if (isParameterizedType(aClass)) {
        holder.registerProblem(aClass.getNameIdentifier(),  WebBeansInspectionBundle.message("SimpleWebBeanInconsistencyInspection.parameterized.class"));
      }

      if (isNonStaticInner(aClass)) {
        holder.registerProblem(aClass.getNameIdentifier(),  WebBeansInspectionBundle.message("SimpleWebBeanInconsistencyInspection.static.inner.class"));
      }

      final String unallowedAncestor = getUnallowedAncestor(aClass);
      if (unallowedAncestor != null) {
        holder.registerProblem(aClass.getNameIdentifier(),  WebBeansInspectionBundle.message("SimpleWebBeanInconsistencyInspection.unallowed.ancessor", unallowedAncestor));
      }


    }
  }

  private static boolean isClassContainsWebBeansAnnotations(final PsiClass psiClass, Module module) {
    final List<String> annos = collectWebBeansAnnotations(module);

    if (AnnotationUtil.findAnnotation(psiClass, annos) != null) return true;

    for (PsiField field : psiClass.getFields()) {
      if (AnnotationUtil.findAnnotation(field, annos) != null) return true;
    }

    for (PsiMethod psiMethod : psiClass.getMethods()) {
      if (AnnotationUtil.findAnnotation(psiMethod, annos) != null) return true;

      for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
        if (AnnotationUtil.findAnnotation(parameter, annos) != null) return true;
      }
    }

    return false;
  }

  private static List<String> collectWebBeansAnnotations(Module module) {
    final List<String> annotations = WebBeansCommonUtils.getAnnotations(WebBeansAnnoConstants.class);

    annotations.addAll(WebBeansCommonUtils.getBindingTypesQualifiedNames(module));
    annotations.addAll(WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getScopeTypesClasses(module)));
    annotations.addAll(WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getDeploymentTypesClasses(module)));
    annotations.addAll(WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getStereotypeAnnotationClasses(module)));

    return annotations;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.simple.bean.inconsistency.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SimpleWebBeanInconsistencyInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}