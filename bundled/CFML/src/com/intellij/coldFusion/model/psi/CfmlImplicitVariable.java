package com.intellij.coldFusion.model.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author vnikolaenko
 */
public class CfmlImplicitVariable extends RenameableFakePsiElement implements CfmlVariable {
    private final PsiComment myComment;
    private final String myName;
    private String myType;

    public CfmlImplicitVariable(@NotNull final PsiFile containingFile, final PsiComment comment, @NotNull final String name) {
        super(containingFile);
        myComment = comment;
        myName = name;
    }

    @Override
    public TextRange getTextRange() {
        return myComment.getTextRange();
    }

    @NotNull
    public String getName() {
        return myName;
    }

    public PsiElement getNavigationElement() {
        return myComment;
    }

    public PsiElement getParent() {
        return myComment;
    }

    public String getTypeName() {
        return "Type name variable";
    }

    public String toString() {
        return "ImplicitVariable " + myName;
    }

    public void setType(final String type) {
        myType = type;
    }

    @Nullable
    public PsiType getPsiType() {
        if (myType == null) {
            return null;
        }
        try {
            if (myType.toLowerCase() == "javaloader") {
              return new CfmlFunctionCallExpression.CfmlJavaLoaderClassType(myComment, getProject());
            }
            return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeFromText(myType, myComment);
        } catch (IncorrectOperationException e) {
            return null;
        }
    }

    public Icon getIcon() {
        return Icons.VARIABLE_ICON;
    }
}
