/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopModel;
import com.intellij.jam.JamService;
import com.intellij.openapi.module.Module;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author peter
 */
public class AopModelImpl implements AopModel {
  private final Module myModule;

  public AopModelImpl(@NotNull final Module module) {
    myModule = module;
  }

  public List<AopAspectImpl> getAspects() {
    return JamService.getJamService(myModule.getProject()).getJamClassElements(AopAspectImpl.ASPECT_META, AopConstants.ASPECT_ANNO, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule));
  }

  public List<AopPointcutImpl> getPointcuts() {
    return JamService.getJamService(myModule.getProject()).getJamMethodElements(AopPointcutImpl.POINTCUT_METHOD_META, AopConstants.POINTCUT_ANNO, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule));
  }

}
