/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlTokenType extends IElementType {

  public VtlTokenType(@NotNull @NonNls final String debugName) {
    super(debugName, VtlLanguage.INSTANCE);
  }

}
