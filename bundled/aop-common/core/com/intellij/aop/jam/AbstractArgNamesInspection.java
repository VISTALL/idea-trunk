/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopBundle;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.AopProvider;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class AbstractArgNamesInspection extends AbstractAopInspection {

  protected void checkAopMethod(final PsiMethod pointcutMethod, final LocalAopModel model, final ProblemsHolder holder,
                                final AopPointcutExpressionFile aopFile) {
    checkAnnotation(pointcutMethod.getParameterList().getParameters(), holder, model.getArgNamesManipulator(), pointcutMethod);
  }

  @Override
  protected void checkElement(final PsiElement element, final ProblemsHolder holder) {
    super.checkElement(element, holder);
    for (final AopProvider provider : Extensions.getExtensions(AopProvider.EXTENSION_POINT_NAME)) {
      final Pair<? extends ArgNamesManipulator,PsiMethod> pair = provider.getCustomArgNamesManipulator(element);
      if (pair != null) {
        final PsiMethod method = pair.second;
        checkAnnotation(method.getParameterList().getParameters(), holder, pair.first, method);
      }
    }
  }

  protected abstract void checkAnnotation(final PsiParameter[] parameters, final ProblemsHolder holder,
                                          final ArgNamesManipulator manipulator, final PsiMethod method);

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return AopBundle.message("inspection.group.display.name.aop");
  }
}
