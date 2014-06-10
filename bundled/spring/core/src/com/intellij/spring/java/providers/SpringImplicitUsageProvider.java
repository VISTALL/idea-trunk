package com.intellij.spring.java.providers;

import com.intellij.aop.jam.AopConstants;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiClass;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.SpringModel;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.List;

public class SpringImplicitUsageProvider implements ImplicitUsageProvider {
  private static final @NonNls List<String> WUSED_SYMBOL_ANNOTATIONS =
      Arrays.asList(SpringAnnotationsConstants.AUTOWIRED_ANNOTATION,
                    SpringAnnotationsConstants.RESOURCE_ANNOTATION,
                    "org.springframework.jmx.export.annotation.ManagedOperation",
                    "org.springframework.jmx.export.annotation.ManagedAttribute",
                    AopConstants.BEFORE_ANNO,
                    AopConstants.AFTER_ANNO,
                    AopConstants.AROUND_ANNO,
                    AopConstants.AFTER_RETURNING_ANNO,
                    AopConstants.AFTER_THROWING_ANNO);

  public boolean isImplicitUsage(final PsiElement element) {
    return isImplicitWrite(element) || isBeanConstructor(element);
  }

  public boolean isImplicitRead(final PsiElement element) {
    return false;
  }

  public boolean isImplicitWrite(final PsiElement element) {
    return element instanceof PsiModifierListOwner &&
           AnnotationUtil.isAnnotated((PsiModifierListOwner)element, WUSED_SYMBOL_ANNOTATIONS);
  }

  private static boolean isBeanConstructor(final PsiElement element) {
    if (element instanceof PsiMethod && ((PsiMethod)element).isConstructor()) {
      final PsiClass containingClass = ((PsiMethod)element).getContainingClass();
      if (containingClass == null) {
        return false;
      }
      final SpringModel springModel = SpringUtils.getModelByPsiElement(element);
      return springModel != null && !springModel.findBeansByPsiClass(containingClass).isEmpty();
    }
    return false;
  }
}