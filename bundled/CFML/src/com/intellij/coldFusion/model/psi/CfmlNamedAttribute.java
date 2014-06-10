package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiType;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
// an element wich declared in <cftag namingAttribute = "here" ... >
public class CfmlNamedAttribute extends CfmlAttribute implements CfmlVariable, PsiNamedElement {
    public CfmlNamedAttribute(@NotNull ASTNode node) {
        super(node);
    }

    public PsiType getPsiType() {
        // should be find by type of nearest parent tag
        return null;
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    public String getName() {
        return getValue();
    }

    public Icon getIcon() {
        return Icons.VARIABLE_ICON;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        PsiElement element = findChildByType(CfmlCompositeElementTypes.ATTRIBUTE_VALUE);
        return element != null ? element : this;
    }
}
