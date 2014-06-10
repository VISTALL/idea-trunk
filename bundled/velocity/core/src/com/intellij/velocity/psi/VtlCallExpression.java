package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 11.06.2008
 */
public abstract class VtlCallExpression extends VtlCompositeElement {

    public VtlCallExpression(@NotNull final ASTNode node) {
        super(node);
    }

    @NotNull
    public VtlReferenceExpression getReferenceExpression() {
        return findNotNullChildByClass(VtlReferenceExpression.class);
    }

    @NotNull
    public VtlCallable[] getCallableCandidates() {
        return getReferenceExpression().getCallableCandidates();
    }

    @Nullable
    public VtlArgumentList findArgumentList() {
        return findChildByClass(VtlArgumentList.class);
    }
}
