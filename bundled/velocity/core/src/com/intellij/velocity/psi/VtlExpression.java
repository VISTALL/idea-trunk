/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public interface VtlExpression extends PsiElement {

    VtlExpression[] EMPTY_ARRAY = new VtlExpression[0];
    @Nullable
    PsiType getPsiType();
}
