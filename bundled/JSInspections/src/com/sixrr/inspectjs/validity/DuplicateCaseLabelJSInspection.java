package com.sixrr.inspectjs.validity;

import com.intellij.lang.javascript.psi.JSCaseClause;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSSwitchStatement;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import com.sixrr.inspectjs.utils.EquivalenceChecker;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DuplicateCaseLabelJSInspection extends JavaScriptInspection {
    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("duplicate.case.label.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.VALIDITY_GROUP_NAME;
    }

    public String buildErrorString(Object... args) {
        return InspectionJSBundle.message("duplicate.case.label.error.string");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor
            extends BaseInspectionVisitor {

        @Override public void visitJSSwitchStatement(@NotNull JSSwitchStatement statement) {
            super.visitJSSwitchStatement(statement);

            final Set<JSExpression> conditions = new HashSet<JSExpression>();
            collectCaseLabels(statement, conditions);
            final int numConditions = conditions.size();
            if (numConditions < 2) {
                return;
            }
            final JSExpression[] conditionArray =
                    conditions.toArray(new JSExpression[numConditions]);
            final boolean[] matched = new boolean[conditionArray.length];
            Arrays.fill(matched, false);
            for (int i = 0; i < conditionArray.length; i++) {
                if (matched[i]) {
                    continue;
                }
                final JSExpression condition = conditionArray[i];
                for (int j = i + 1; j < conditionArray.length; j++) {
                    if (matched[j]) {
                        continue;
                    }
                    final JSExpression testCondition = conditionArray[j];
                    final boolean areEquivalent =
                            EquivalenceChecker.expressionsAreEquivalent(condition,
                                    testCondition);
                    if (areEquivalent) {
                        registerError(testCondition);
                        if (!matched[i]) {
                            registerError(condition);
                        }
                        matched[i] = true;
                        matched[j] = true;
                    }
                }
            }
            int numDefaults = 0;
            final JSCaseClause[] clauses = statement.getCaseClauses();
            for (JSCaseClause clause : clauses) {
                if (clause.isDefault()) {
                    numDefaults++;
                }
            }
            if (numDefaults > 1) {
                for (JSCaseClause clause : clauses) {
                    if (clause.isDefault()) {
                        registerError(clause.getFirstChild());
                    }
                }
            }
        }

        private static void collectCaseLabels(JSSwitchStatement statement,
                                              Set<JSExpression> conditions) {
            final JSCaseClause[] clauses = statement.getCaseClauses();
            for (JSCaseClause clause : clauses) {
                if (!clause.isDefault()) {
                    final JSExpression caseExpression = clause.getCaseExpression();
                    if (caseExpression != null) {
                        conditions.add(caseExpression);
                    }
                }
            }
        }
    }
}
