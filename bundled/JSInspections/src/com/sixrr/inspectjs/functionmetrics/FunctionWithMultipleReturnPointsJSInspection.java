package com.sixrr.inspectjs.functionmetrics;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.sixrr.inspectjs.*;
import com.sixrr.inspectjs.utils.ControlFlowUtils;
import org.jetbrains.annotations.NotNull;

public class FunctionWithMultipleReturnPointsJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("function.with.multiple.return.points.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.FUNCTIONMETRICS_GROUP_NAME;
    }

    public String buildErrorString(Object... args) {
        final JSFunction function = (JSFunction) ((PsiElement) args[0]).getParent();
        assert function != null;
        final int returnPointCount = countReturnPoints(function);
        if (functionHasIdentifier(function)) {
            return InspectionJSBundle.message("function.contains.multiple.return.points.error.string", returnPointCount);
        } else {
            return InspectionJSBundle.message("anonymous.function.contains.multiple.return.points.error.string", returnPointCount);
        }
    }

    private static int countReturnPoints(JSFunction function) {
        final PsiElement lastChild = function.getLastChild();
        if (!(lastChild instanceof JSBlockStatement)) {
            return 0;
        }
        boolean hasFallthroughReturn = false;
        if (ControlFlowUtils.statementMayCompleteNormally((JSStatement) lastChild)) {
            hasFallthroughReturn = true;
        }
        final ReturnCountVisitor visitor = new ReturnCountVisitor();
        lastChild.accept(visitor);

        final int returnCount = visitor.getReturnCount();
        if (hasFallthroughReturn) {
            return returnCount + 1;
        } else {
            return returnCount;
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor extends BaseInspectionVisitor {

        @Override public void visitJSFunctionDeclaration(@NotNull JSFunction function) {
            final int returnPointCount = countReturnPoints(function);
            if (returnPointCount <= 1) {
                return;
            }
            registerFunctionError(function);
        }
    }

    private static class ReturnCountVisitor extends JSRecursiveElementVisitor {
        private int returnCount = 0;

        @Override public void visitJSElement(JSElement jsElement) {
            int oldCount = 0;
            if (jsElement instanceof JSFunction) {
                oldCount = returnCount;
            }
            super.visitJSElement(jsElement);

            if (jsElement instanceof JSFunction) {
                returnCount = oldCount;
            }
        }

        @Override public void visitJSReturnStatement(JSReturnStatement statement) {
            super.visitJSReturnStatement(statement);
            returnCount++;
        }

        public int getReturnCount() {
            return returnCount;
        }
    }
}

