package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.psi.directives.VtlDirectiveImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 29.04.2008
 */
public class VtlDirectiveType extends VtlCompositeElementType {
    private final String myPresentableName;

    private final boolean myNeedsClosing;

    public VtlDirectiveType(@NotNull final String debugName, @NotNull final String presentableName, boolean needsClosing) {
        super(debugName);
        myPresentableName = presentableName;
        myNeedsClosing = needsClosing;
    }

    public VtlDirectiveType(@NotNull final String debugName) {
        super(debugName);
        myPresentableName = null;
        myNeedsClosing = false;
    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return new VtlDirectiveImpl(node, myPresentableName, myNeedsClosing);
    }

}
