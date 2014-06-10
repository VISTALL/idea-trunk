package com.intellij.webBeans.manager;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.webBeans.beans.*;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeansFactory {
  private final Module myModule;

  public WebBeansFactory(Module module) {
    myModule = module;
  }

  @Nullable
  public WebBeanPsiClassDescriptor createWebBeanDescriptor(final PsiClass psiClass) {
    if (WebBeansCommonUtils.isSimpleWebBean(psiClass)) {
      return new SimpleWebBeanDescriptor(psiClass);
    }
    return null;
  }

  @Nullable
  public ProducerBeanDescriptor createProducerWebBeanDescriptor(@NotNull PsiMember member) {
    if (AnnotationUtil.isAnnotated(member, WebBeansAnnoConstants.PRODUCES_ANNOTATION, true)) {
      if (member instanceof PsiMethod) return new MethodProducerBeanDescriptor((PsiMethod)member);
      if (member instanceof PsiField) return new FieldProducerBeanDescriptor((PsiField)member);
    }
    return null;
  }
}
