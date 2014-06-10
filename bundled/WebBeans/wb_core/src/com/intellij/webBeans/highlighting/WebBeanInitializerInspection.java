package com.intellij.webBeans.highlighting;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import com.intellij.webBeans.beans.SimpleWebBeanDescriptor;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.codeInsight.AnnotationUtil;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeanInitializerInspection extends BaseWebBeanInspection {
  @Override
  protected void checkSimpleWebBean(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
    checkInitializerConstructor(descriptor, holder);
    checkInitializerMethods(descriptor, holder);

  }

  private static void checkInitializerMethods(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
    List<PsiMethod> methods = descriptor.getInitializerMethods();

    // org.jboss.webbeans.bean.AbstractClassBean#initInitializerMethods()
    for (PsiMethod method : methods) {
      checkWrongAnnotations(method, holder);
      checkNonStatic(method, holder);
      checkWrongAnnotatedParameters(method, holder);
    }
  }

  private static void checkWrongAnnotatedParameters(PsiMethod method, ProblemsHolder holder) {
    for (PsiParameter parameter : method.getParameterList().getParameters()) {
      checkWrongAnnotatedParameter(method, holder, WebBeansAnnoConstants.DISPOSES_ANNOTATION, parameter);
      checkWrongAnnotatedParameter(method, holder, WebBeansAnnoConstants.OBSERVES_ANNOTATION, parameter);
    }
  }

  private static void checkWrongAnnotatedParameter(PsiMethod method, ProblemsHolder holder, String anno, PsiParameter parameter) {
    if (AnnotationUtil.isAnnotated(parameter, anno, true)) {
       holder.registerProblem(method.getNameIdentifier(),
                           WebBeansInspectionBundle.message("WebBeanInitializerInspection.initializer.with.disposes.parameters", anno));
     }
  }

  private static void checkNonStatic(PsiMethod method, ProblemsHolder holder) {
    if (method.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
      holder.registerProblem(method.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanInitializerInspection.initializer.method.cannot.be.static"));
    }
  }

  private static void checkWrongAnnotations(PsiMethod method, ProblemsHolder holder) {
    checkWrongAnnotation(method, WebBeansAnnoConstants.PRODUCES_ANNOTATION, holder);
  }

  private static void checkWrongAnnotation(PsiMethod method, String annotation, ProblemsHolder holder) {
    if (AnnotationUtil.isAnnotated(method, annotation, true)) {
      holder.registerProblem(method.getNameIdentifier(),
                             WebBeansInspectionBundle.message("WebBeanInitializerInspection.wrong.initializer.method.annotation",
                                                              annotation));

    }
  }

  private static void checkInitializerConstructor(SimpleWebBeanDescriptor descriptor, ProblemsHolder holder) {
    List<PsiMethod> methods = descriptor.getInitializerConstructors();
    if (methods.size() > 1) {
      for (PsiMethod method : methods) {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(method, WebBeansAnnoConstants.INITIALIZER_ANNOTATION);
        assert annotation != null;

        holder.registerProblem(annotation,
                               WebBeansInspectionBundle.message("WebBeanInitializerInspection.more.than.one.initializer.constructor",
                                                                methods.size()));
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.initializer.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanInitializerInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}
