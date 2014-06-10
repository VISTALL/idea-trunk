package com.intellij.webBeans.providers;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.webBeans.utils.WebBeansCommonUtils;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeansImplicitUsageProvider implements ImplicitUsageProvider {

  public boolean isImplicitUsage(PsiElement element) {
    if (element instanceof PsiModifierListOwner) {
      Module module = ModuleUtil.findModuleForPsiElement(element);
      if(module != null && WebBeansCommonUtils.isWebBeansFacetDefined(module)) {
          return AnnotationUtil.isAnnotated((PsiModifierListOwner)element,WebBeansCommonUtils.getBindingTypesQualifiedNames(module));
      }
    }
    return false;
  }

  public boolean isImplicitRead(final PsiElement element) {
    return isImplicitUsage(element);
  }

  public boolean isImplicitWrite(final PsiElement element) {
    return isImplicitUsage(element);
  }
}

