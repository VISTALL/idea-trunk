package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlFunctionCallExpression;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfmlReferenceInspection extends CfmlInspectionBase {

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    public boolean isEnabledByDefault() {
        return true;
    }


    protected void registerProblems(PsiElement element, ProblemsHolder holder) {
        if (!(element instanceof CfmlReferenceExpression)) {
            return;
        }
        if (CfmlUtil.getPredifinedFunctions().contains(((CfmlReferenceExpression)element).getText().toLowerCase())) {
            return;
        }
        final CfmlReferenceExpression ref = (CfmlReferenceExpression) element;
        // final boolean resolvedWithError = results.length > 0 && !results[0].isValidResult();

        if (/*resolvedWithError || */ref.resolve() == null && !(ref.getParent() instanceof CfmlFunctionCallExpression &&
        CfmlUtil.isPredefinedFunction(((CfmlFunctionCallExpression)ref.getParent()).getFunctionName()))) {
            final String message = "Can't resolve";
            holder.registerProblem(ref, message, /*resolvedWithError ? */GENERIC_ERROR_OR_WARNING /*: LIKE_UNKNOWN_SYMBOL*/);
        }
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return CfmlBundle.message("cfml.references.inspection");
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "CfmlReferenceInspection";
    }
}
