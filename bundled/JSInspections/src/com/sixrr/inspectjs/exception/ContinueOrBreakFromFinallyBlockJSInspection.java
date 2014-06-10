package com.sixrr.inspectjs.exception;

import com.intellij.lang.javascript.psi.JSBreakStatement;
import com.intellij.lang.javascript.psi.JSContinueStatement;
import com.intellij.lang.javascript.psi.JSStatement;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import com.sixrr.inspectjs.utils.ControlFlowUtils;
import org.jetbrains.annotations.NotNull;

public class ContinueOrBreakFromFinallyBlockJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("continue.or.break.inside.finally.block.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.ERRORHANDLING_GROUP_NAME;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public String buildErrorString(Object... args) {
        return InspectionJSBundle.message("continue.or.break.inside.finally.block.error.string");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor extends BaseInspectionVisitor {

        @Override public void visitJSContinueStatement(@NotNull JSContinueStatement statement) {
            super.visitJSContinueStatement(statement);
            if (!ControlFlowUtils.isInFinallyBlock(statement)) {
                return;
            }
            final JSStatement continuedStatement = statement.getStatementToContinue();
            if (continuedStatement == null) {
                return;
            }
            if (ControlFlowUtils.isInFinallyBlock(continuedStatement)) {
                return;
            }
            registerStatementError(statement);
        }

        @Override public void visitJSBreakStatement(@NotNull JSBreakStatement statement) {
            super.visitJSBreakStatement(statement);
            if (!ControlFlowUtils.isInFinallyBlock(statement)) {
                return;
            }
            final JSStatement exitedStatement = statement.getStatementToBreak();
            if (exitedStatement == null) {
                return;
            }
            if (ControlFlowUtils.isInFinallyBlock(exitedStatement)) {
                return;
            }
            registerStatementError(statement);
        }
    }
}
