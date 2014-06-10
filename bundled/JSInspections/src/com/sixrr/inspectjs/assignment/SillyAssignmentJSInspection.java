package com.sixrr.inspectjs.assignment;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSAssignmentExpression;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.sixrr.inspectjs.BaseInspectionVisitor;
import com.sixrr.inspectjs.InspectionJSBundle;
import com.sixrr.inspectjs.JSGroupNames;
import com.sixrr.inspectjs.JavaScriptInspection;
import com.sixrr.inspectjs.utils.EquivalenceChecker;
import org.jetbrains.annotations.NotNull;

public class SillyAssignmentJSInspection
        extends JavaScriptInspection {


    @NotNull
    public String getDisplayName() {
        return InspectionJSBundle.message("silly.assignment.display.name");
    }

    @NotNull
    public String getGroupDisplayName() {
        return JSGroupNames.ASSIGNMENT_GROUP_NAME;
    }

    public String buildErrorString(Object... args) {
        return InspectionJSBundle.message("silly.assignment.error.string");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new Visitor();
    }

    private static class Visitor
            extends BaseInspectionVisitor {

        @Override public void visitJSAssignmentExpression(@NotNull JSAssignmentExpression assignment) {
            super.visitJSAssignmentExpression(assignment);

            final IElementType sign = assignment.getOperationSign();
            if (!JSTokenTypes.EQ.equals(sign)) {
                return;
            }
            JSExpression lhs = assignment.getLOperand();
            if(lhs instanceof JSDefinitionExpression)
            {
                lhs = ((JSDefinitionExpression)lhs).getExpression();
            }
            final JSExpression rhs = assignment.getROperand();
            if(rhs == null || lhs == null)
            {
                return;
            }
            if(!(rhs instanceof JSReferenceExpression) ||
                    !(lhs instanceof JSReferenceExpression) )
            {
                return;
            }
            final JSReferenceExpression rhsReference = (JSReferenceExpression) rhs;
            final JSReferenceExpression lhsReference = (JSReferenceExpression) lhs;
            final JSExpression rhsQualifier = rhsReference.getQualifier();
            final JSExpression lhsQualifier = lhsReference.getQualifier();
            if(rhsQualifier !=null || lhsQualifier !=null)
            {
                if(!EquivalenceChecker.expressionsAreEquivalent(rhsQualifier, lhsQualifier))
                {
                    return;
                }
            }
            final String rhsName = rhsReference.getReferencedName();
            final String lhsName = lhsReference.getReferencedName();
            if(rhsName == null || lhsName == null)
            {
                return;
            }
            if(!rhsName.equals(lhsName))
            {
                return;
            }
            final PsiElement rhsReferent = rhsReference.resolve();
            final PsiElement lhsReferent = lhsReference.resolve();
            if(rhsReferent != null && lhsReferent != null &&
                    !rhsReferent.equals(lhsReferent))
            {
                return;
            }

            if (lhsName.equals("location") && lhsQualifier != null && lhsQualifier.getText().equals("document")) {
              // document.location = document.location causes browser refresh
              return;
            }
            registerError(assignment);
        }
    }
}
