package com.intellij.webBeans.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.jam.WebBeansJamModel;
import com.intellij.webBeans.jam.specialization.WebBeansSpecializes;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeanSpecializesInspection extends BaseWebBeanInspection {

  @Override
  protected void checkClass(PsiClass psiClass, ProblemsHolder holder, @NotNull Module module) {
    List<WebBeansSpecializes> specializeses = WebBeansJamModel.getModel(module).getWebBeansSpecializeses(psiClass);

    for (WebBeansSpecializes specializes : specializeses) {
      if (specializes instanceof WebBeansSpecializes.ProducerMethodMapping) {
        checkSpecializesMethodNonStatic(holder, (WebBeansSpecializes.ProducerMethodMapping)specializes);
        checkSpecializesMethodIsProducerMethod(holder, (WebBeansSpecializes.ProducerMethodMapping)specializes);
        checkOverridesMethodOfSuperclass(holder, (WebBeansSpecializes.ProducerMethodMapping)specializes);
      }

      if (specializes instanceof WebBeansSpecializes.ClassMapping) {
        checkHasSpecializedBeans((WebBeansSpecializes.ClassMapping)specializes, holder);
      }

      checkMultipleSpecializedBeans(specializes, holder, module);
      checkDuplicateNamedAnnotations(specializes, holder);
      // checkDeploymentTypePrecedence(specializes, holder, module); //todo Compare Precedence !!!!
    }
  }

  private static void checkOverridesMethodOfSuperclass(ProblemsHolder holder, WebBeansSpecializes.ProducerMethodMapping methodMapping) {
    PsiMethod specializedMember = methodMapping.getSpecializedMember();
    if (specializedMember == null || !AnnotationUtil.isAnnotated(specializedMember, WebBeansAnnoConstants.PRODUCES_ANNOTATION, true)) {
      holder.registerProblem(methodMapping.getAnnotation(), WebBeansInspectionBundle.message(
        "WebBeanSpecializesInspection.specializes.method.must.override.producer.of.superclass"));
    }
  }

  private static void checkDeploymentTypePrecedence(WebBeansSpecializes specializes, ProblemsHolder holder, Module module) {
    final PsiClass specializesDeploymentType = getDeploymentType(specializes.getPsiElement(), module);
    if (specializesDeploymentType == null) return;

    final PsiClass specializedDeploymentType = getDeploymentType(specializes.getSpecializedMember(), module);
    if (specializedDeploymentType == null) return;

    if (!hasHigherPrecedence(specializesDeploymentType, specializedDeploymentType)) {
      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specializes.deployment.type.precedence"));

    }

  }

  private static boolean hasHigherPrecedence(final PsiClass deploymentType1, final PsiClass deploymentType2) {
    //todo Compare Precedence !!!!
    return true;
  }

  @Nullable
  private static PsiClass getDeploymentType(@Nullable PsiMember psiMember, Module module) {
    if (psiMember == null) return null;

    return null;
  }


  private static void checkDuplicateNamedAnnotations(WebBeansSpecializes specializes, ProblemsHolder holder) {
    PsiMember psiClass = specializes.getPsiElement();
    if (hasNamedAnno(psiClass) && hasNamedAnno(specializes.getSpecializedMember())) {
      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specialized.class.has.duplicated.named.anno"));
    }
  }

  private static boolean hasNamedAnno(@Nullable PsiMember psiMember) {
    return psiMember != null && AnnotationUtil.isAnnotated(psiMember, WebBeansAnnoConstants.NAMED_ANNOTATION, false);
  }

  private static void checkHasSpecializedBeans(WebBeansSpecializes.ClassMapping specializes, ProblemsHolder holder) {
    PsiClass specializedBean = specializes.getSpecializedMember();

    if (specializedBean == null || CommonClassNames.JAVA_LANG_OBJECT.equals(specializedBean.getQualifiedName())) {
      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specialized.class.not.defined"));
    }
  }

  private static void checkMultipleSpecializedBeans(WebBeansSpecializes specializes, ProblemsHolder holder, Module module) {
    PsiMember specializedMember = specializes.getSpecializedMember();

    if (specializedMember == null) return;

    Set<String> brotherSpecializes = new HashSet<String>();
    for (WebBeansSpecializes beansSpecializes : WebBeansJamModel.getModel(module).getWebBeansSpecializeses()) {
      if (beansSpecializes.equals(specializes)) continue;

      if (specializedMember.equals(beansSpecializes.getSpecializedMember())) {
        PsiClass containingClass = beansSpecializes.getContainingClass();
        if (containingClass != null) {
          brotherSpecializes.add(containingClass.getName());
        }
      }
    }

    if (brotherSpecializes.size() > 0) {
      String names = StringUtil.join(brotherSpecializes.toArray(new String[brotherSpecializes.size()]), ",");

      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specialized.class.has.multiple.specializes",
                                                              names));
    }
  }

  private static void checkSpecializesMethodIsProducerMethod(ProblemsHolder holder, WebBeansSpecializes.ProducerMethodMapping specializes) {
    if (!AnnotationUtil.isAnnotated(specializes.getPsiElement(), WebBeansAnnoConstants.PRODUCES_ANNOTATION, false)) {
      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specializes.method.must.be.producer"));
    }
  }


  private static void checkSpecializesMethodNonStatic(ProblemsHolder holder, WebBeansSpecializes.ProducerMethodMapping specializes) {
    if (specializes.getPsiElement().getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
      holder.registerProblem(specializes.getAnnotation(),
                             WebBeansInspectionBundle.message("WebBeanSpecializesInspection.specializes.method.must.be.non.static"));
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.specialize.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanSpecializesInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}