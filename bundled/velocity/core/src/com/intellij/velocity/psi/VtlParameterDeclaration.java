/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.Icons;
import com.intellij.velocity.VelocityBundle;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlParameterDeclaration extends VtlPresentableNamedElement implements VtlVariable {

    public VtlParameterDeclaration(final ASTNode node) {
        super(node);
    }

    public String getTypeName() {
        return VelocityBundle.message("type.name.macro.parameter");
    }

    public Icon getIcon() {
        return Icons.PARAMETER_ICON;
    }

    @Nullable
    public PsiType getPsiType() {
        return null;
    }
}
