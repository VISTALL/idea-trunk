package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.psi.VtlArgumentList;
import com.intellij.velocity.psi.VtlCallExpression;
import static com.intellij.velocity.psi.VtlCompositeElementTypes.REFERENCE_EXPRESSION;
import com.intellij.velocity.psi.VtlExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlMacroCall extends VtlCallExpression implements VtlDirective {
    public VtlMacroCall(ASTNode node) {
        super(node);
    }

    @NotNull
    public String getPresentableName() {
        PsiElement child = findChildByType(REFERENCE_EXPRESSION);
        return child != null ? child.getText() : "";
    }

    public int getFoldingStartOffset() {
        return getNode().getTextRange().getStartOffset() + getPresentableName().length() + 1;
    }

    public int getFoldingEndOffset() {
        return getNode().getTextRange().getEndOffset();
    }

    public boolean needsClosing() {
        return false;
    }

    @NotNull
    public VtlExpression[] getArguments() {
        VtlArgumentList argList = findChildByClass(VtlArgumentList.class);
        if (argList == null) {
            return VtlExpression.EMPTY_ARRAY;
        }
        return argList.getArguments();
    }
}
