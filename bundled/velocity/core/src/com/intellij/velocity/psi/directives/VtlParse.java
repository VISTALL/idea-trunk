package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlArgumentList;
import com.intellij.velocity.psi.VtlLiteralExpressionType;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlParse extends VtlFileReferenceDirective {
    public VtlParse(@NotNull final ASTNode node) {
        super(node, "parse");
    }

    @Nullable
    public VtlFile resolveFile() {
        VtlArgumentList argumentList = getArgumentList();
        if (argumentList == null) {
            return null;
        }
        final PsiElement literal = argumentList.getFirstChild();
        if (literal == null) {
            return null;
        }
        if (!(literal instanceof VtlLiteralExpressionType.VtlStringLiteral)) {
            return null;
        }
        return PsiUtil.findFile(literal.getReferences(), VtlFile.class);
    }
}
