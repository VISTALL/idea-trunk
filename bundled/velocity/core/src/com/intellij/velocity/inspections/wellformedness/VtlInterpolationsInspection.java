/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections.wellformedness;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.inspections.VtlInspectionBase;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlParameterDeclaration;
import com.intellij.velocity.psi.directives.VtlAssignment;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlInterpolationsInspection extends VtlInspectionBase {

    protected void registerProblems(PsiElement element, ProblemsHolder holder) {
        if (element instanceof VtlParameterDeclaration) {
            PsiElement wouldBeFormalNotationStart = element.getFirstChild();
            if(PsiUtil.isFormalNotationStart(wouldBeFormalNotationStart)) {
                registerFormalNotationProblem(wouldBeFormalNotationStart.getNextSibling(), holder);
            }
        } else if (element instanceof VtlAssignment) {
            PsiElement wouldBeVariableDeclared = ((VtlAssignment) element).getAssignedVariableElement();
            if (wouldBeVariableDeclared != null
                    && PsiUtil.isFormalNotationStart(wouldBeVariableDeclared.getPrevSibling())) {
                registerFormalNotationProblem(wouldBeVariableDeclared, holder);
            }
        }
    }

    private static void registerFormalNotationProblem(PsiElement element, ProblemsHolder holder) {
        holder.registerProblem(element, VelocityBundle.message("vtl.formal.notation.is.not.allowed"),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return VelocityBundle.message("vtl.welformedness.inspection");
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "VtlInterpolationsInspection";
    }
}