/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiFile;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.VtlArgumentList;
import com.intellij.velocity.psi.VtlExpression;
import com.intellij.velocity.psi.VtlLiteralExpressionType;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.directives.VtlParse;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/**
 * @author Alexey Chmutov
 */
public class VtlFileReferencesInspection extends VtlInspectionBase {

  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (element instanceof VtlParse) {
      VtlArgumentList argumentList = ((VtlParse)element).getArgumentList();
      if (argumentList == null) {
        return;
      }
      VtlExpression[] arguments = argumentList.getArguments();
      final String message = VelocityBundle.message("vtl.only.first.parse.argument.will.be.parsed");
      for (int i = 1; i < arguments.length; i++) {
        holder.registerProblem(arguments[i], message, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
      }
    }
    else {
      if (element instanceof VtlLiteralExpressionType.VtlStringLiteral) {
        final PsiFile file = element.getContainingFile();
        if (file instanceof VtlFile && ((VtlFile)file).isIdeTemplateFile()) {
          return;
        }
        for (final PsiReference reference : element.getReferences()) {
          if (reference.resolve() != null) {
            continue;
          }
          final String message =
              MessageFormat.format(((EmptyResolveMessageProvider)reference).getUnresolvedMessagePattern(), reference.getCanonicalText());
          holder.registerProblem(reference, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
        }
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return VelocityBundle.message("vtl.file.references.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "VtlFileReferencesInspection";
  }
}