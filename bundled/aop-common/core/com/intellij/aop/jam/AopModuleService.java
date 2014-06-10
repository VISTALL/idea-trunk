/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAspect;
import com.intellij.aop.AopModel;
import com.intellij.aop.AopPointcut;
import com.intellij.jam.JamService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class AopModuleService implements Disposable {
  private final NotNullLazyValue<AopModelImpl> myModel;
  private static final AopModel EMPTY_AOP_MODEL = new AopModel() {
    public List<? extends AopAspect> getAspects() {
      return Collections.emptyList();
    }

    public List<? extends AopPointcut> getPointcuts() {
      return Collections.emptyList();
    }
  };

  @NotNull
  public static AopModuleService getService(@NotNull Module module) {
    return ModuleServiceManager.getService(module, AopModuleService.class);
  }

  @NotNull
  public static AopModel getAopModel(@Nullable Module module) {
    return module == null ? EMPTY_AOP_MODEL : getService(module).getModel();
  }

  public AopModuleService(final @NotNull Module module) {
    myModel = new NotNullLazyValue<AopModelImpl>() {
      @NotNull
      protected AopModelImpl compute() {
        return new AopModelImpl(module);
      }
    };
  }

  @Nullable
  public static AopAspectImpl getAspect(@NotNull PsiClass cls) {
    return JamService.getJamService(cls.getProject()).getJamElement(AopAspectImpl.class, cls);
  }

  @Nullable
  public static AopAdviceImpl getAdvice(@NotNull PsiMethod method) {
    return JamService.getJamService(method.getProject()).getJamElement(AopAdviceImpl.class, method);
  }
  @Nullable
  public static AopPointcutImpl getPointcut(@NotNull PsiMethod method) {
    return JamService.getJamService(method.getProject()).getJamElement(AopPointcutImpl.class, method);
  }
  @Nullable
  public static AopIntroductionImpl getIntroduction(@NotNull PsiField field) {
    return JamService.getJamService(field.getProject()).getJamElement(AopIntroductionImpl.class, field);
  }

  @NotNull
  public synchronized AopModelImpl getModel() {
    return myModel.getValue();
  }

  public void dispose() {
  }
}
