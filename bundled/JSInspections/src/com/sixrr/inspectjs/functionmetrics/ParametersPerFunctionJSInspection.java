package com.sixrr.inspectjs.functionmetrics;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.psi.PsiElement;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import org.jetbrains.annotations.NotNull;

public class ParametersPerFunctionJSInspection
        extends FunctionMetricsInspection {
    @NotNull
    public String getID() {
        return "OverlyComplexFunctionJS";
    }

    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("function.with.too.many.parameters.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.FUNCTIONMETRICS_GROUP_NAME;
    }

    protected int getDefaultLimit() {
        return 5;
    }

    protected String getConfigurationLabel() {
        return InspectionJSBundle.message("function.parameter.limit");
    }

    public String buildErrorString(Object... args) {
        final JSFunction function = (JSFunction) ((PsiElement) args[0]).getParent();
        assert function != null;
        final JSParameterList parameterList = function.getParameterList();
        final JSParameter[] parameters = parameterList.getParameters();
        final int numParameters = parameters.length;
        if (functionHasIdentifier(function)) {
            return InspectionJSBundle.message("function.has.too.many.parameters.error.string", numParameters);
        } else {
            return InspectionJSBundle.message("anonymous.function.has.too.many.parameters.error.string", numParameters);
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private class Visitor extends BaseInspectionVisitor {

        @Override public void visitJSFunctionDeclaration(@NotNull JSFunction function) {
            final JSParameterList parameterList = function.getParameterList();
            if (parameterList == null) {
                return;
            }
            final JSParameter[] parameters = parameterList.getParameters();
            if (parameters == null) {
                return;
            }
            final int numParameters = parameters.length;
            if (numParameters <= getLimit()) {
                return;
            }
            registerFunctionError(function);
        }
    }
}

