/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public interface AopTypeExpression extends AopPatternContainer {

  AopPointcutExpressionFile getContainingFile();

  @Nullable
  @NonNls 
  String getTypePattern();
}
