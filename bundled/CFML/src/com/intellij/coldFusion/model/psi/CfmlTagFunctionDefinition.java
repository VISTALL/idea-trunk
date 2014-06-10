package com.intellij.coldFusion.model.psi;

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
public class CfmlTagFunctionDefinition extends CfmlTag implements PsiNamedElement {
    public CfmlTagFunctionDefinition(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        CfmlNamedAttribute attribute = findChildByClass(CfmlNamedAttribute.class);
        return attribute != null ? attribute.getValue() : null;
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public boolean isDeclarativeInside() {
        return false;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        PsiElement namedAttribute = findChildByClass(CfmlNamedAttribute.class);
        return namedAttribute != null ? namedAttribute.getNavigationElement() : this;
    }
}
