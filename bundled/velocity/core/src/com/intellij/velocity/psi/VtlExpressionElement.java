package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
*/
class VtlExpressionElement extends VtlCompositeElement implements VtlExpression {
    public VtlExpressionElement(@NotNull final ASTNode node) {
        super(node);
    }

    public PsiType getPsiType() {
        VtlExpression childExpression = findChildByClass(VtlExpression.class);
        if (childExpression != null) {
            return childExpression.getPsiType();
        }
        return null;
    }
}
