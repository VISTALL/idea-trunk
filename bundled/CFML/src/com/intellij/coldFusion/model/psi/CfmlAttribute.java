package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

/**
 * Created by Lera Nikolaenko
 * Date: 05.11.2008
 */
public class CfmlAttribute extends CfmlCompositeElement implements PsiNamedElement {
    public CfmlAttribute(ASTNode astNode) {
        super(astNode);
    }

    public String getValue() {
        PsiElement element = findChildByType(CfmlCompositeElementTypes.ATTRIBUTE_VALUE);
        return element == null ? "" : element.getText();
    }

    public String getName() {
        return findChildByType(CfmlTokenTypes.ATTRIBUTE).getText();
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }
}

