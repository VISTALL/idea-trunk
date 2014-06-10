package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public abstract class VtlNamedElement extends VtlCompositeElement implements PsiNamedElement {
    public VtlNamedElement(@NotNull final ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public final String getName() {
        PsiElement e = getNameElement();
        if (e == null) {
            return null;
        }
        return e.getText();
    }

    @Nullable
    protected PsiElement getNameElement() {
        // may return null only for an element containing syntax errors
        return findChildByType(VtlElementTypes.IDENTIFIER);
    }

    @NotNull
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        PsiElement nameElement = getNameElement();
        assert nameElement != null;
        final PsiElement newNameElement = PsiUtil.createIdentifierElement(getProject(), name);
        nameElement.replace(newNameElement);
        return this;
    }

    @Override
    public int getTextOffset() {
        PsiElement e = getNameElement();
        if (e == null) {
            return super.getTextOffset();
        }
        return e.getTextOffset();
    }
}
