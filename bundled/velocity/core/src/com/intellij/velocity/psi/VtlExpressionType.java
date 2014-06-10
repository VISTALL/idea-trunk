package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */

public class VtlExpressionType extends VtlCompositeElementType {

    public VtlExpressionType(@NotNull @NonNls final String debugName) {
        super(debugName);
    }

    public PsiElement createPsiElement(ASTNode node) {
        return new VtlExpressionElement(node);
    }

}

