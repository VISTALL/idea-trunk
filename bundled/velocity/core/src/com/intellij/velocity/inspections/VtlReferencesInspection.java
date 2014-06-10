/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlReferencesInspection extends VtlInspectionBase {

  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (!(element instanceof VtlReferenceExpression)) {
      return;
    }
    final VtlReferenceExpression ref = (VtlReferenceExpression)element;
    if (!ref.isQualifierResolved()) {
      return;
    }
    final PsiFile file = ref.getContainingFile();
    if (file instanceof VtlFile && ((VtlFile)file).isIdeTemplateFile()) {
      return;
    }
    final ResolveResult[] results = ref.multiResolve(false);
    final boolean resolvedWithError = results.length > 0 && !results[0].isValidResult();

    if (resolvedWithError || ref.resolve() == null) {
      final String message = ref.getUnresolvedMessage(resolvedWithError);
      holder.registerProblem(ref, message, resolvedWithError ? GENERIC_ERROR_OR_WARNING : LIKE_UNKNOWN_SYMBOL);
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return VelocityBundle.message("vtl.references.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "VtlReferencesInspection";
  }
}