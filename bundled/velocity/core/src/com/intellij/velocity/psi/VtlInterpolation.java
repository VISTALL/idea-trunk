package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlInterpolation extends VtlExpressionElement {
    public VtlInterpolation(@NotNull final ASTNode node) {
        super(node);
    }
}
