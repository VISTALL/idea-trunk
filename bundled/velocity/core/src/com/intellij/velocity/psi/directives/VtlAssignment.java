/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.VtlElementTypes;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public abstract class VtlAssignment extends VtlDirectiveImpl {

    private final NotNullLazyValue<CachedValue<AssignedVariable>> myAssignedVariable;

    protected VtlAssignment(final ASTNode node, @NotNull String presentableName, boolean needsClosing) {
        super(node, presentableName, needsClosing);
        myAssignedVariable = new NotNullLazyValue<CachedValue<AssignedVariable>>() {
            @NotNull
            protected CachedValue<AssignedVariable> compute() {
                return getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<AssignedVariable>() {
                    public Result<AssignedVariable> compute() {
                        return Result.create(createAssignedVariable(), PsiModificationTracker.MODIFICATION_COUNT);
                    }
                }, false);
            }
        };
    }

    @Nullable
    private AssignedVariable createAssignedVariable() {
        VtlReferenceExpression varElement = getAssignedVariableElement();
        if (varElement == null) {
            return null;
        }
        if (getContainingFile().findImplicitVariable(varElement.getReferenceName()) != null) {
            return null;
        }
        return new AssignedVariable();
    }

    @Nullable
    public VtlReferenceExpression getAssignedVariableElement() {
        PsiElement element = findChildByType(VtlElementTypes.REFERENCE_EXPRESSION);
        if (element == null) {
            return null;
        }
        VtlReferenceExpression expression = (VtlReferenceExpression) element;
        return expression.hasQualifier() ? null : expression;
    }

    @Nullable
    public PsiElement getAssignedMethodCallExpression() {
        return findChildByType(VtlElementTypes.METHOD_CALL_EXPRESSION);
    }

    @Nullable
    public abstract PsiType getAssignedVariableElementType();

    @NotNull
    public String getPresentableName() {
        String dirName = super.getPresentableName();
        PsiElement nameElement = findChildByType(VtlElementTypes.REFERENCE_EXPRESSION);
        return nameElement == null ? dirName : dirName + " '" + nameElement.getText() + "'";
    }

    public VtlVariable getAssignedVariable() {
        return myAssignedVariable.getValue().getValue();
    }

    private class AssignedVariable extends RenameableFakePsiElement implements VtlVariable {

        public AssignedVariable() {
            super(VtlAssignment.this.getContainingFile());
        }

        @NotNull
        public String getName() {
            final VtlReferenceExpression expression = getAssignedVariableElement();
            assert expression != null;
            return expression.getReferenceName();
        }

        public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
            VtlReferenceExpression nameElement = getAssignedVariableElement();
            assert nameElement != null;
            nameElement.handleElementRename(name);
            return this;
        }

        @Nullable
        @Override
        public PsiElement getNavigationElement() {
            final VtlReferenceExpression expression = getAssignedVariableElement();
            assert expression != null;
            return expression.getReferenceNameElement();
        }

        public PsiElement getParent() {
            return getAssignedVariableElement();
        }

        public String getTypeName() {
            return VelocityBundle.message("type.name.variable");
        }

        public Icon getIcon() {
            return Icons.VARIABLE_ICON;
        }

        public PsiType getPsiType() {
            return getAssignedVariableElementType();
        }

        public boolean isEquivalentTo(final PsiElement another) {
            if (!getClass().isInstance(another)) {
                return false;
            }
            AssignedVariable other = (AssignedVariable) another;
            return getName().equals(other.getName())
                    && getContainingFile().equals(other.getContainingFile());
        }

        public String toString() {
            return "AssignedVariable " + getName();
        }
    }
}