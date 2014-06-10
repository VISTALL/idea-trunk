package com.intellij.webBeans.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.javaee.model.annotations.AnnotationGenericValue;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.beans.AbstractWebBeanDescriptor;
import com.intellij.webBeans.beans.WebBeanDescriptor;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.constants.WebBeansCommonConstants;
import com.intellij.webBeans.resources.WebBeansInspectionBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class WebBeanStereotypeRestrictionsInspection extends BaseWebBeanInspection {

  @Override
  protected void checkWebBeanDescriptor(WebBeanDescriptor descriptor, ProblemsHolder holder) {
    Set<PsiClass> stereotypes = descriptor.getStereotypes();
    for (PsiClass stereotype : stereotypes) {
      final PsiAnnotation stereotypeAnnotation = AnnotationUtil.findAnnotation(stereotype, WebBeansAnnoConstants.STEREOTYPE_ANNOTATION);
      if (stereotypeAnnotation != null) {
        checkRequiredTypes(descriptor, getRequiredClasses(stereotypeAnnotation, WebBeansCommonConstants.STEREOTYPE_REQUARED_TYPES_PARAM),
                           holder, stereotype);

        checkSupportedScopes(descriptor,
                             getRequiredClasses(stereotypeAnnotation, WebBeansCommonConstants.STEREOTYPE_SUPPORTED_SCOPES_PARAM), holder,
                             stereotype);
      }
    }
    checkMultipleStereotypeScopesCollision(descriptor, stereotypes, holder);
  }

  private static void checkMultipleStereotypeScopesCollision(WebBeanDescriptor descriptor,
                                                             Set<PsiClass> stereotypes,
                                                             ProblemsHolder holder) {
    PsiMember psiMember = ((AbstractWebBeanDescriptor)descriptor).getAnnotatedItem();

    Module module = ModuleUtil.findModuleForPsiElement(psiMember);

    if (module == null || hasOwnScopeAnnotation(psiMember, module) || stereotypes.size() < 2) return;

    if (hasDifferentScopesInStereotypes(stereotypes, module)) {
      final PsiElement psiElement =
        psiMember instanceof PsiNameIdentifierOwner ? ((PsiNameIdentifierOwner)psiMember).getNameIdentifier() : psiMember;
      holder.registerProblem(psiElement, WebBeansInspectionBundle.message("WebBeanStereotypeRestrictionsInspection.differnt.scopes.in.stereotypes"));

    }
  }

  private static boolean hasDifferentScopesInStereotypes(Set<PsiClass> stereotypes, Module module) {
    String commonScopeAnno = null;
    Collection<String> scopeTypesClasses = WebBeansCommonUtils.getScopeQualifiedNames(module);

    for (PsiClass stereotype : stereotypes) {
      PsiAnnotation annotation = AnnotationUtil.findAnnotation(stereotype, scopeTypesClasses);
      if (annotation != null) {
        if (commonScopeAnno == null) {
          commonScopeAnno = annotation.getQualifiedName();
        }
        else {
          if (!commonScopeAnno.equals(annotation.getQualifiedName())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean hasOwnScopeAnnotation(PsiMember psiMember, Module module) {
    Collection<String> qualifiedNames = WebBeansCommonUtils.getScopeQualifiedNames(module);
    Collection<String> sterotypesNames = WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getStereotypeAnnotationClasses(module));

    Collection<String> nonStereotypedAnnos = new ArrayList<String>();
    for (String qualifiedName : qualifiedNames) {
        if(!sterotypesNames.contains(qualifiedName)) {
          nonStereotypedAnnos.add(qualifiedName);
        }
    }

    return AnnotationUtil.findAnnotations(psiMember, nonStereotypedAnnos).length > 0;
  }

  private static void checkSupportedScopes(WebBeanDescriptor descriptor,
                                           Set<PsiClass> supportedScopes,
                                           ProblemsHolder holder,
                                           PsiClass stereotype) {
    if (supportedScopes.size() == 0) return;
    
    PsiMember psiMember = ((AbstractWebBeanDescriptor)descriptor).getAnnotatedItem();

    PsiType type = descriptor.getType();
    if (type == null) return;

    final PsiClass scopeType = descriptor.getScopeType();

    if (scopeType == null) return;

    if (!supportedScopes.contains(scopeType)) {
      PsiAnnotation annotation = AnnotationUtil.findAnnotation(psiMember, scopeType.getQualifiedName());

      if (annotation != null) {
        holder.registerProblem(annotation, WebBeansInspectionBundle.message("WebBeanStereotypeRestrictionsInspection.scope.isnot.allowed",
                                                                            "@" + scopeType.getName(),
                                                                            "@" + stereotype.getName()));
      }
    }
  }

  private static void checkRequiredTypes(WebBeanDescriptor descriptor,
                                         Set<PsiClass> requiredTypes,
                                         ProblemsHolder holder,
                                         PsiClass stereotype) {
    PsiMember psiMember = ((AbstractWebBeanDescriptor)descriptor).getAnnotatedItem();

    PsiType type = descriptor.getType();
    if (type == null) return;

    PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiMember.getProject());
    for (PsiClass requiredType : requiredTypes) {

      if (!factory.createType(requiredType).isAssignableFrom(type)) {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(psiMember, stereotype.getQualifiedName());

        if (annotation != null) {
          holder.registerProblem(annotation, WebBeansInspectionBundle.message(
            "WebBeanStereotypeRestrictionsInspection.required.types.isnot.implemented", requiredType.getQualifiedName(),
            type.getCanonicalText()));
        }
      }
    }
  }

  private static Set<PsiClass> getRequiredClasses(final PsiAnnotation stereotypeAnnotation, final String attributeName) {
    Set<PsiClass> requiredClasses = new HashSet<PsiClass>();
    if (stereotypeAnnotation != null) {
      List<AnnotationGenericValue<PsiClass>> arrayValue = AnnotationModelUtil.getPsiClassArrayValue(stereotypeAnnotation, attributeName);

      for (AnnotationGenericValue<PsiClass> psiClassValue : arrayValue) {
        PsiClass psiClass = psiClassValue.getValue();
        if (psiClass != null) {
          requiredClasses.add(psiClass);
        }
      }
    }
    return requiredClasses;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebBeansInspectionBundle.message("inspection.name.stereotype.restriction.errors");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebBeanStereotypeRestrictionsInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}