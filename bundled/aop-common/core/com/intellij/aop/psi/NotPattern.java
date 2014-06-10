/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.util.Processor;

/**
 * @author peter
 */
public class NotPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myInnerPattern;

  public NotPattern(final AopPsiTypePattern innerPattern) {
    myInnerPattern = innerPattern;
  }

  public AopPsiTypePattern getInnerPattern() {
    return myInnerPattern;
  }

  public boolean accepts(@NotNull PsiType type) {
    return !myInnerPattern.accepts(type);
  }

  public boolean accepts(@NotNull final String qualifiedName) {
    return !myInnerPattern.accepts(qualifiedName);
  }

  public boolean processPackages(final PsiManager manager, final Processor<PsiPackage> processor) {
    return TRUE.processPackages(manager, processor);
  }
}
