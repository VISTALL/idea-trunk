package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.velocity.psi.VtlArgumentList;
import com.intellij.velocity.psi.VtlCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlDirectiveImpl extends VtlCompositeElement implements VtlDirective {

    @NotNull
    private final String myPresentableName;

    private final boolean myNeedsClosing;

    public VtlDirectiveImpl(@NotNull final ASTNode node, @NotNull String presentableName, boolean needsClosing) {
        super(node);
        myPresentableName = presentableName;
        myNeedsClosing = needsClosing;
    }

    @NotNull
    public String getPresentableName() {
        return myPresentableName;
    }

    public int getFoldingStartOffset() {
        return getNode().getTextRange().getStartOffset() + myPresentableName.length() + 1;
    }

    public int getFoldingEndOffset() {
        return getNode().getTextRange().getEndOffset() - (needsClosing() ? "#end".length() : 0);
    }

    public boolean needsClosing() {
        return myNeedsClosing;
    }

    @Nullable
    public VtlArgumentList getArgumentList() {
        return findChildByClass(VtlArgumentList.class);
    }
}
