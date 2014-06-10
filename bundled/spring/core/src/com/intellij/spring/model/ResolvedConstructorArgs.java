/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Avdeev
 */
public interface ResolvedConstructorArgs {

  boolean isResolved();

  @Nullable
  PsiMethod getResolvedMethod();

  @Nullable
  List<PsiMethod> getCheckedMethods();

  /**
   * @return null if {@link #getResolvedMethod()} returns null.
   */
  @Nullable
  Map<ConstructorArg, PsiParameter> getResolvedArgs();

  Map<ConstructorArg, PsiParameter> getResolvedArgs(@NotNull PsiMethod method);

  Map<PsiParameter, Collection<SpringBaseBeanPointer>> getAutowiredParams(@NotNull PsiMethod method);
}
