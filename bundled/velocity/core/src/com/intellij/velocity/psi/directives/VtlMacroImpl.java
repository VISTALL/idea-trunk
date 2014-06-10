/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.velocity.Icons;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlMacroImpl extends VtlPresentableNamedElement implements VtlDirective, VtlMacro {

    public VtlMacroImpl(@NotNull final ASTNode node) {
        super(node);
    }

    public boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state, final PsiElement lastParent,
                                       @NotNull final PsiElement place) {
        if (!super.processDeclarations(processor, state, lastParent, place)) {
            return false;
        }
        for (final VtlVariable declaration : getParameters()) {
            if (!processor.execute(declaration, state)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected PsiElement getNameElement() {
        return findHeaderOfDirective().findChildByType(VtlElementTypes.IDENTIFIER);
    }

    @Nullable
    public TextRange getNameElementRange() {
        PsiElement nameElement = getNameElement();
        return nameElement == null ? null : nameElement.getTextRange();
    }

    @NotNull
    public String getPresentableName() {
        return "macro '" + getName() + "'";
    }

    @NotNull
    public VtlParameterDeclaration[] getParameters() {
        return findHeaderOfDirective().findChildrenByClass(VtlParameterDeclaration.class);
    }

    public boolean isDeprecated() {
        return false;
    }

    public int getFoldingStartOffset() {
        return getNode().getTextRange().getStartOffset() + "#macro".length();
    }

    public int getFoldingEndOffset() {
        return getNode().getTextRange().getEndOffset() - "#end".length();
    }

    public boolean needsClosing() {
        return true;
    }

    public String getTypeName() {
        return VelocityBundle.message("type.name.macro");
    }

    public Icon getIcon() {
        return Icons.SHARP_ICON;
    }
}