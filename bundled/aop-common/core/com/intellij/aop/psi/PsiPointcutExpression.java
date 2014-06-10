/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.PsiMember;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author peter
 */
public interface PsiPointcutExpression extends AopPatternContainer {
  @NotNull
  PointcutMatchDegree acceptsSubject(final PointcutContext context, PsiMember member);

  @NotNull
  AopPointcutExpressionFile getContainingFile();
}
