/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author peter
 */
public class AopPointcutExpressionFileType extends LanguageFileType {
  public static final AopPointcutExpressionFileType INSTANCE = new AopPointcutExpressionFileType();
  
  private AopPointcutExpressionFileType() {
    super(new AopPointcutExpressionLanguage());
  }

  @NotNull
  @NonNls
  public String getName() {
    return "Pointcut Expression";
  }

  @NotNull
  public String getDescription() {
    return getName();
  }

  @NotNull
  @NonNls
  public String getDefaultExtension() {
    return "pointcutExpression";
  }

  @Nullable
  public Icon getIcon() {
    return null;
  }
}
