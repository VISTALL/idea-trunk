package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfmlUniqueDefsInspection extends CfmlInspectionBase {
    protected void registerProblems(PsiElement element, ProblemsHolder holder) {
        /*
        if (!(element instanceof CfmlDefinitionExpression)) {
            return;
        }
        Set<String> functionsDefined =
                CfmlPsiUtil.getFunctionDefinitionBefore(element.getContainingFile(), ((CfmlDefinitionExpression)element).getName(), element.getTextRange().getStartOffset());
        final boolean resolvedWithError = functionsDefined.size() > 0;

        if (resolvedWithError) {
            final String message = "Already defined";
            holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
        }
        */
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return CfmlBundle.message("cfml.unique.defs.inspection");
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "CfmlUniqueDefsInspection";
    }
}

