package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlDirectiveHeader extends VtlCompositeElement {
    public VtlDirectiveHeader(@NotNull final ASTNode node) {
        super(node);
    }

    @Nullable
    public PsiElement findChildByType(IElementType type) {
        return super.findChildByType(type);
    }

    @NotNull
    public <T> T[] findChildrenByClass(Class<T> aClass) {
        return super.findChildrenByClass(aClass);
    }

    @Nullable
    public <T> T findChildByClass(Class<T> aClass) {
        return super.findChildByClass(aClass);
    }
}
