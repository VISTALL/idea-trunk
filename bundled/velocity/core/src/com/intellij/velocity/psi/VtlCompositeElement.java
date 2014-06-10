/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.velocity.Icons;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlCompositeElement extends ASTWrapperPsiElement {

    public VtlCompositeElement(@NotNull final ASTNode node) {
        super(node);
    }

    public boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state,@Nullable final PsiElement lastParent,
                                       @NotNull final PsiElement place) {
        return PsiUtil.processDeclarations(processor, state, lastParent, null, this);
    }

    @NotNull
    public VtlDirective[] getDirectiveChildren() {
        return findChildrenByClass(VtlDirective.class);
    }

    public VtlCompositeElement[] getCompositeChildren() {
        return findChildrenByClass(VtlCompositeElement.class);
    }

    public VtlFile getContainingFile() {
        return (VtlFile) super.getContainingFile();
    }

    public Icon getIcon(final int flags) {
        return Icons.SHARP_ICON;
    }

    public String toString() {
        return getNode().getElementType().toString();
    }

    @Nullable
    protected VtlDirectiveHeader findHeaderOfDirective() {
        return super.findChildByClass(VtlDirectiveHeader.class);
    }
}
