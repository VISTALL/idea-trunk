/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlArgumentList extends VtlCompositeElement {
    public VtlArgumentList(final ASTNode node) {
        super(node);
    }

    @NotNull
    public VtlExpression[] getArguments() {
        return findChildrenByClass(VtlExpression.class);
    }
}
