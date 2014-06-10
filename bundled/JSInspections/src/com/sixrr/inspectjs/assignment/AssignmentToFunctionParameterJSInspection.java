package com.sixrr.inspectjs.assignment;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssignmentToFunctionParameterJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("assignment.to.function.parameter.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.ASSIGNMENT_GROUP_NAME;
    }

    @Nullable
    protected String buildErrorString(Object... args) {
        return InspectionJSBundle.message("assignment.to.function.parameter.error.string");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor extends BaseInspectionVisitor {
        @Override public void visitJSAssignmentExpression(JSAssignmentExpression jsAssignmentExpression) {
            super.visitJSAssignmentExpression(jsAssignmentExpression);
            final JSExpression lhs = jsAssignmentExpression.getLOperand();
            checkOperand(lhs);
        }

        @Override public void visitJSPrefixExpression(JSPrefixExpression jsPrefixExpression) {
            super.visitJSPrefixExpression(jsPrefixExpression);
            final IElementType sign = jsPrefixExpression.getOperationSign();
            if (!JSTokenTypes.PLUSPLUS.equals(sign) &&
                    !JSTokenTypes.MINUSMINUS.equals(sign)) {
                return;
            }
            final JSExpression operand = jsPrefixExpression.getExpression();
            checkOperand(operand);
        }

        @Override public void visitJSPostfixExpression(JSPostfixExpression jsPostfixExpression) {
            super.visitJSPostfixExpression(jsPostfixExpression);
            final IElementType sign = jsPostfixExpression.getOperationSign();
            if (!JSTokenTypes.PLUSPLUS.equals(sign) &&
                    !JSTokenTypes.MINUSMINUS.equals(sign)) {
                return;
            }
            final JSExpression operand = jsPostfixExpression.getExpression();
            checkOperand(operand);
        }

        private void checkOperand(JSExpression operand) {
            if (operand == null) {
                return;
            }
            if (operand instanceof JSDefinitionExpression) {
                final JSExpression definiend = ((JSDefinitionExpression) operand).getExpression();
                if (definiend instanceof JSReferenceExpression) {
                    final PsiElement referent = ((PsiReference) definiend).resolve();
                    if (referent == null) {
                        return;
                    }
                    if (!(referent instanceof JSParameter)) {
                        return;
                    }
                    registerError(operand);
                }
            }
            if (operand instanceof JSReferenceExpression) {
                final PsiElement referent = ((PsiReference) operand).resolve();
                if (referent == null) {
                    return;
                }
                if (!(referent instanceof JSParameter)) {
                    return;
                }
                registerError(operand);
            }
        }
    }
}
