/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.highlighting;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopBundle;
import com.intellij.aop.AopProvider;
import com.intellij.aop.jam.AopConstants;
import com.intellij.codeInspection.*;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.aop.SpringAopProvider;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author peter
 */
public class MissingAspectjAutoproxyInspection extends LocalInspectionTool{
  private final NotNullLazyValue<SpringAopProvider> mySpringAopProvider = new NotNullLazyValue<SpringAopProvider>() {
    @NotNull
    protected SpringAopProvider compute() {
      final AopProvider[] providers = Extensions.getExtensions(AopProvider.EXTENSION_POINT_NAME);
      for (final AopProvider provider : providers) {
        if (provider instanceof SpringAopProvider) {
          return (SpringAopProvider)provider;
        }
      }
      throw new AssertionError();
    }
  };

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  public ProblemDescriptor[] checkFile(@NotNull final PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    if (file instanceof PsiJavaFile) {
      final Module module = ModuleUtil.findModuleForPsiElement(file);
      if (module == null || SpringUtils.isSpring25(module)) {
        return null;
      }

      for (final PsiClass aClass : ((PsiJavaFile)file).getClasses()) {
        final PsiModifierList modifierList = aClass.getModifierList();
        if (modifierList != null) {
          final PsiAnnotation annotation = modifierList.findAnnotation(AopConstants.ASPECT_ANNO);
          if (annotation != null) {
            final AopAdvisedElementsSearcher searcher = mySpringAopProvider.getValue().getAdvisedElementsSearcher(aClass);
            if (searcher instanceof SpringAdvisedElementsSearcher) {
              final List<SpringModel> models = ((SpringAdvisedElementsSearcher)searcher).getSpringModels();
              if (!models.isEmpty() && !isAspectJSupportEnabled(models)) {
                final LocalQuickFix[] fixes = models.isEmpty() ? LocalQuickFix.EMPTY_ARRAY : new LocalQuickFix[]{new EnableAspectJQuickFix(
                  models.get(0))};
                return new ProblemDescriptor[]{
                  manager.createProblemDescriptor(annotation.getNameReferenceElement(), SpringBundle.message("aop.warning.aspectj.isnt.enabled"),
                                                  fixes, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)};
              }
            }
          }
        }
      }
    }

    return super.checkFile(file, manager, isOnTheFly);
  }


  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return AopBundle.message("inspection.group.display.name.aop");
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("missing.aspectj.autoproxy.inspection.display.name");
  }

  @NotNull
  public String getShortName() {
    return "MissingAspectjAutoproxyInspection";
  }

  public static boolean isAspectJSupportEnabled(List<SpringModel> models) {
    for (final SpringModel model : models) {
      for (final DomFileElement<Beans> fileElement : model.getRoots()) {
        for (final CommonSpringBean springBean : SpringUtils.getChildBeans(fileElement.getRootElement(), false)) {
          final PsiClass beanClass = springBean.getBeanClass();
          if (beanClass != null && SpringConstants.ASPECTJ_AUTOPROXY_BEAN_CLASS.equals(beanClass.getQualifiedName())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
