package com.sixrr.inspectjs.confusing;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSIfStatement;
import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.inspectjs.*;
import com.sixrr.inspectjs.utils.BoolUtils;
import com.sixrr.inspectjs.utils.ParenthesesUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NegatedIfStatementJSInspection extends JavaScriptInspection {
    private final NegatedIfElseFix fix = new NegatedIfElseFix();

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("negated.if.statement.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.CONFUSING_GROUP_NAME;
    }

    @Nullable
    protected String buildErrorString(Object... args) {
        return InspectionJSBundle.message("negated.ref.statement.error.string");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    protected InspectionJSFix buildFix(PsiElement location) {
        return fix;
    }

    private static class NegatedIfElseFix extends InspectionJSFix {

        @NotNull
        public String getName() {
            return InspectionJSBundle.message("invert.if.condition.fix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiElement ifToken = descriptor.getPsiElement();
            final JSIfStatement ifStatement = (JSIfStatement) ifToken.getParent();
            assert ifStatement != null;
            final JSStatement elseBranch = ifStatement.getElse();
            final JSStatement thenBranch = ifStatement.getThen();
            final JSExpression condition = ifStatement.getCondition();
            final String negatedCondition =
                    BoolUtils.getNegatedExpressionText(condition);
            String elseText = elseBranch.getText();
            final PsiElement lastChild = elseBranch.getLastChild();
            if (lastChild instanceof PsiComment) {
                final PsiComment comment = (PsiComment) lastChild;
                final IElementType tokenType = comment.getTokenType();
                if (JSTokenTypes.END_OF_LINE_COMMENT.equals(tokenType)) {
                    elseText += '\n';
                }
            }
            @NonNls final String newStatement = "if(" + negatedCondition + ')' +
                    elseText + " else " + thenBranch.getText();
            replaceStatement(ifStatement, newStatement);
        }
    }
    private static class Visitor extends BaseInspectionVisitor {


        @Override public void visitJSIfStatement(JSIfStatement statement) {
            super.visitJSIfStatement(statement);
            final PsiElement parent = statement.getParent();
            if (parent instanceof JSIfStatement) {
                final JSIfStatement parentStatement = (JSIfStatement) parent;
                final JSStatement elseBranch = parentStatement.getElse();
                if (statement.equals(elseBranch)) {
                    return;
                }
            }
            if (statement.getElse() == null) {
                return;
            }


            JSExpression condition = statement.getCondition();
            condition = ParenthesesUtils.stripExpression(condition);
            if (condition == null || (!BoolUtils.isNegation(condition) && !isNotEquals(condition))) {
                return;
            }
            registerStatementError(statement);
        }

        private boolean isNotEquals(JSExpression expression) {
            if (!(expression instanceof JSBinaryExpression)) {
                return false;
            }
            final JSBinaryExpression binaryExpression =
                    (JSBinaryExpression) expression;
            final IElementType sign = binaryExpression.getOperationSign();
            return JSTokenTypes.NE.equals(sign) || JSTokenTypes.NEQEQ.equals(sign);
        }
    }
}
