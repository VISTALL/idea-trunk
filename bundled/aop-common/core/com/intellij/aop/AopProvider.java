/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author peter
 */
public abstract class AopProvider {
  public static final ExtensionPointName<AopProvider> EXTENSION_POINT_NAME = ExtensionPointName.create("com.intellij.aop.provider");

  @NotNull
  public Set<? extends AopAspect> getAdditionalAspects(@NotNull Module module) {
    return Collections.emptySet();
  }

  @Nullable
  public abstract AopAdvisedElementsSearcher getAdvisedElementsSearcher(@NotNull PsiClass aClass);

  @Nullable
  public Pair<? extends ArgNamesManipulator, PsiMethod> getCustomArgNamesManipulator(@NotNull PsiElement element) {
    return null;
  }

  @Nullable
  public Integer getAdviceOrder(AopAdvice advice) {
    return null;
  }

}
