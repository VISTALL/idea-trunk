package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
public class CfmlFunctionDefinition extends CfmlCompositeElement implements PsiNamedElement {
    public CfmlFunctionDefinition(@NotNull ASTNode node) {
        super(node);
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    public PsiElement getReferenceElement() {
        return findChildByType(CfscriptTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        PsiElement element = getReferenceElement();
        return element != null ? element.getText() : null;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        PsiElement element = getReferenceElement();
        return element != null ? element : this;
    }
}
