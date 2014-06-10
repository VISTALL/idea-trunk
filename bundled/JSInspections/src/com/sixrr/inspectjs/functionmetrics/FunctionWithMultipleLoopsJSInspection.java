package com.sixrr.inspectjs.functionmetrics;

import com.intellij.lang.javascript.psi.JSBlockStatement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.psi.PsiElement;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import org.jetbrains.annotations.NotNull;

public class FunctionWithMultipleLoopsJSInspection extends JavaScriptInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("function.with.multiple.loops.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.FUNCTIONMETRICS_GROUP_NAME;
    }

    public String buildErrorString(Object... args) {
        final JSFunction function = (JSFunction) ((PsiElement) args[0]).getParent();
        assert function != null;
        final LoopCountVisitor visitor = new LoopCountVisitor();
        final PsiElement lastChild = function.getLastChild();
        assert lastChild != null;
        lastChild.accept(visitor);
        final int loopCount = visitor.getCount();
        if (functionHasIdentifier(function)) {
            return InspectionJSBundle.message("function.contains.multiple.loops.error.string", loopCount);
        }
        else {
            return InspectionJSBundle.message("anonymous.function.contains.multiple.loops.error.string", loopCount);
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor extends BaseInspectionVisitor {

        @Override public void visitJSFunctionDeclaration(@NotNull JSFunction function) {
            // note: no call to super
            final PsiElement lastChild = function.getLastChild();
            if (!(lastChild instanceof JSBlockStatement)) {
                return;
            }
            final LoopCountVisitor visitor = new LoopCountVisitor();
            lastChild.accept(visitor);
            final int negationCount = visitor.getCount();
            if (negationCount <= 1) {
                return;
            }
            registerFunctionError(function);
        }
    }
}

