package com.sixrr.inspectjs.bugs;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RecursionVisitor extends JSRecursiveElementVisitor {

    private boolean recursive = false;
    private final JSFunction function;
    private final String functionName;

    public RecursionVisitor(@NotNull JSFunction function) {
        super();
        this.function = function;
        functionName = function.getName();
    }

    @Override public void visitElement(@NotNull PsiElement element) {
        if (!recursive) {
            super.visitElement(element);
        }
    }

    @Override public void visitJSCallExpression(
            @NotNull JSCallExpression call) {
        if (recursive) {
            return;
        }
        super.visitJSCallExpression(call);
        final JSExpression methodExpression = call.getMethodExpression();

        // method expression could be e.g. a["1"]
        if (!(methodExpression instanceof JSReferenceExpression)){
            return;
        }
        final JSReferenceExpression functionExpression = (JSReferenceExpression)methodExpression;
        final String calledFunctionName = functionExpression.getReferencedName();

        if (calledFunctionName == null) {
            return;
        }
        if (!calledFunctionName.equals(functionName)) {
            return;
        }
        final PsiElement referent = functionExpression.resolve();
        if (!function.equals(referent)) {
            return;
        }
        final JSExpression qualifier = functionExpression.getQualifier();
        if (qualifier == null || qualifier instanceof JSThisExpression) {
            recursive = true;
        }
    }

    public boolean isRecursive() {
        return recursive;
    }
}
