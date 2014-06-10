package com.intellij.velocity.inspections;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static com.intellij.codeInspection.ProblemHighlightType.INFO;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import static com.intellij.velocity.VelocityBundle.message;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlExpression;
import com.intellij.velocity.psi.VtlLoopVariable;
import com.intellij.velocity.psi.VtlOperatorExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.06.2008
 */
public class VtlTypesInspection extends VtlInspectionBase {
    protected void registerProblems(PsiElement element, ProblemsHolder holder) {
        if (element instanceof VtlOperatorExpression) {
            final VtlOperatorExpression expression = (VtlOperatorExpression) element;
            if (expression.getPsiType() != null) {
                return;
            }
            String message = expression.getIndefiniteTypeMessage();
            if (message != null) {
                holder.registerProblem(expression, message, INFO);
            }
        } else if (element instanceof VtlLoopVariable) {
            final VtlLoopVariable loopVariable = (VtlLoopVariable) element;
            if (loopVariable.getPsiType() != null) {
                return;
            }
            VtlExpression expression = loopVariable.getIterableExpression();
            if (expression == null) {
                return;
            }
            final PsiType type = expression.getPsiType();
            if (type == null) {
                return;
            }
            String typeName = PsiUtil.getPresentableText(type);
            holder.registerProblem(expression, message("illegal.iterable.expression.type", typeName), GENERIC_ERROR_OR_WARNING);
        }
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return message("vtl.types.inspection");
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "VtlTypesInspection";
    }
}
