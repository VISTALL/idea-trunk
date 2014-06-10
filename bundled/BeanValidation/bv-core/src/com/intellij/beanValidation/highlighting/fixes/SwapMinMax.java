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

package com.intellij.beanValidation.highlighting.fixes;

import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class SwapMinMax extends BaseBVQuickFix{
  private final PsiElement minElement;
  private final PsiElement maxElement;

  public SwapMinMax(PsiElement minElement, PsiElement maxElement) {
    super(BVInspectionBundle.message("swap.min.max"));
    this.minElement = minElement;
    this.maxElement = maxElement;
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement maxCopy = maxElement.copy();
    final PsiElement minCopy = minElement.copy();
    maxElement.replace(minCopy);
    minElement.replace(maxCopy);
  }
}
