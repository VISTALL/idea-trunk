/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public interface SpringPropertyDefinition extends SpringValueHolderDefinition {
  @Nullable @NonNls
  String getPropertyName();

  @Nullable
  PsiType[] getTypesByValue();

}
