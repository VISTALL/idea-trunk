/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlMethodCallExpression extends VtlCallExpression implements VtlExpression {
    public VtlMethodCallExpression(final ASTNode node) {
        super(node);
    }

    @NotNull
    public VtlArgumentList getArgumentList() {
        return findNotNullChildByClass(VtlArgumentList.class);
    }

    public PsiType[] getArgumentTypes() {
        VtlExpression[] args = getArgumentList().getArguments();
        return ContainerUtil.map2Array(args, PsiType.class, new NullableFunction<VtlExpression, PsiType>() {
            public PsiType fun(final VtlExpression expression) {
                return expression.getPsiType();
            }
        });
    }

    @Nullable
    public PsiType getPsiType() {
        return getReferenceExpression().getPsiType();
    }
}
