package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlFileReferenceDirective extends VtlDirectiveImpl {

    public VtlFileReferenceDirective(@NotNull final ASTNode node, @NotNull String presentableName) {
        super(node, presentableName, false);
    }
}
