package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public class CfmlCompositeElement extends ASTWrapperPsiElement {
    public CfmlCompositeElement(@org.jetbrains.annotations.NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    public Language getLanguage() {
        return CfmlLanguage.INSTANCE;
    }

    public CfmlFile getContainingFile() {
        return (CfmlFile) super.getContainingFile();
    }

    @NotNull
    @SuppressWarnings({ "ConstantConditions", "EmptyMethod" })
    public ASTNode getNode() {
        return super.getNode();
    }

    public String toString() {
        return getNode().getElementType().toString();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this);
    }
}
