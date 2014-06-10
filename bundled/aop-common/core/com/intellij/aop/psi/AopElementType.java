/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AopElementType extends IElementType {
  public AopElementType(@NotNull @NonNls String debugName) {
    super(debugName, AopPointcutExpressionLanguage.getInstance());
  }
}
