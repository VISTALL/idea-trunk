package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: vnikolaenko
 * Date: 28.04.2009
 */
public class CfmlAssignment extends CfmlCompositeElement {
    private AssignedVariable myAssignedVariable = null;
    private boolean myVariableWasInitialized = false;

    public CfmlAssignment(@org.jetbrains.annotations.NotNull ASTNode node) {
        super(node);
        /*
        myAssignedVariable = new NotNullLazyValue<AssignedVariable>() {
            @NotNull
            protected AssignedVariable compute() {
                return createAssignedVariable();
            }
        };
        */
    }

    @Nullable
    private AssignedVariable createAssignedVariable() {
        CfmlReferenceExpression varElement = getAssignedVariableElement();
        if (varElement == null) {
            return null;
        }
        CfmlImplicitVariable var = getContainingFile().findImplicitVariable(varElement.getReferenceName());
        if (var != null && var.getTextRange().getStartOffset() < this.getTextRange().getStartOffset()) {
            return null;
        }
        return new AssignedVariable();
    }

    @Nullable
    public CfmlReferenceExpression getAssignedVariableElement() {
        PsiElement element = findChildByType(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);
        if (element == null) {
            return null;
        }
        CfmlReferenceExpression expression = (CfmlReferenceExpression) element;
        return expression;
    }

    @Nullable
    public PsiType getAssignedVariableElementType() {
        CfmlExpression[] expressions = findChildrenByClass(CfmlExpression.class);
        if (expressions.length != 2) {
            return null;
        }
        return expressions[1].getPsiType();
    }

    @NotNull
    public String getPresentableName() {
        PsiElement nameElement = findChildByType(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);
        return nameElement.getText();
    }

    public CfmlVariable getAssignedVariable() {
        if (!myVariableWasInitialized) {
            myVariableWasInitialized = true;
            myAssignedVariable = createAssignedVariable();
        }
        
        return myAssignedVariable;
    }

    private class AssignedVariable extends RenameableFakePsiElement implements CfmlVariable {

        public AssignedVariable() {
            super(CfmlAssignment.this.getContainingFile());
        }

        @NotNull
        public String getName() {
            final CfmlReferenceExpression expression = getAssignedVariableElement();
            if (expression == null) {
              return "";
            }
            return expression.getReferenceName() != null ? expression.getReferenceName() : "";
        }

        public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
            CfmlReferenceExpression nameElement = getAssignedVariableElement();
            assert nameElement != null;
            nameElement.handleElementRename(name);
            return this;
        }

        @Nullable
        @Override
        public PsiElement getNavigationElement() {
            final CfmlReferenceExpression expression = getAssignedVariableElement();
            assert expression != null;
            return expression.getReferenceNameElement();
        }

        public PsiElement getParent() {
            return getAssignedVariableElement();
        }

        public String getTypeName() {
            return "Unknown type";
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
