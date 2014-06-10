/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlCompositeElementType extends IElementType implements ICompositeElementType {

    public VtlCompositeElementType(@NotNull @NonNls final String debugName) {
        super(debugName, VtlLanguage.INSTANCE);
    }

    public PsiElement createPsiElement(ASTNode node) {
        return new VtlCompositeElement(node);
    }

    @NotNull
    public ASTNode createCompositeNode() {
        return new CompositeElement(this);
    }
}