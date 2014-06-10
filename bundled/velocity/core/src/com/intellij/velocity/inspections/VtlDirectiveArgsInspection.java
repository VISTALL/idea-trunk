/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.velocity.inspections;

import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNUSED_SYMBOL;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import static com.intellij.velocity.VelocityBundle.message;
import com.intellij.velocity.psi.directives.VtlSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class VtlDirectiveArgsInspection extends VtlInspectionBase {
  protected void registerProblems(PsiElement element, ProblemsHolder holder) {
    if (element instanceof VtlSet) {
      VtlSet vtlSet = (VtlSet)element;
      String msg = null;
      if (vtlSet.getAssignedMethodCallExpression() != null) {
        holder.registerProblem(vtlSet.getFirstChild(), message("assignment.to.method.call"), LIKE_UNUSED_SYMBOL);
      }
      else {
        PsiType assignedType = vtlSet.getAssignedVariableElementType();
        if (PsiType.VOID.equals(assignedType)) {
          holder.registerProblem(vtlSet.getFirstChild(), message("assignment.of.void"), LIKE_UNUSED_SYMBOL);
        }
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return message("vtl.directive.args.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "VtlDirectiveArgsInspection";
  }
}