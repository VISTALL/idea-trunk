package com.sixrr.inspectjs.confusing;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSPostfixExpression;
import com.intellij.lang.javascript.psi.JSPrefixExpression;
import com.intellij.psi.tree.IElementType;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfusingPlusesOrMinusesJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("confusing.pluses.or.minuses.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.CONFUSING_GROUP_NAME;
    }

    @Nullable
    protected String buildErrorString(Object... args) {
        return InspectionJSBundle.message("confusing.pluses.or.minuses.error.string");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor extends BaseInspectionVisitor {

        @Override public void visitJSBinaryExpression(JSBinaryExpression jsBinaryExpression) {
            super.visitJSBinaryExpression(jsBinaryExpression);
            final IElementType sign = jsBinaryExpression.getOperationSign();
            if (!JSTokenTypes.PLUS.equals(sign) && !JSTokenTypes.MINUS.equals(sign)) {
                return;
            }
            final JSExpression rhs = jsBinaryExpression.getROperand();
            if (rhs == null) {
                return;
            }
            final JSExpression lhs = jsBinaryExpression.getLOperand();
            if (lhs == null) {
                return;
            }
            if (lhs instanceof JSPostfixExpression) {
                final JSPostfixExpression postfixExpression = (JSPostfixExpression) lhs;
                final IElementType innerSign = postfixExpression.getOperationSign();
                if (matches(sign, innerSign)) {
                    registerError(jsBinaryExpression);
                    return;
                }
            }
            if (rhs instanceof JSPrefixExpression) {
                final JSPrefixExpression postfixExpression = (JSPrefixExpression) rhs;
                final IElementType innerSign = postfixExpression.getOperationSign();
                if (matches(sign, innerSign)) {
                    registerError(jsBinaryExpression);
                }
            }
        }

        private static boolean matches(IElementType sign, IElementType innerSign) {
            if (JSTokenTypes.PLUS.equals(sign)) {
                return JSTokenTypes.PLUSPLUS.equals(innerSign);
            } else {
                return JSTokenTypes.MINUSMINUS.equals(innerSign);
            }
        }
    }
}
