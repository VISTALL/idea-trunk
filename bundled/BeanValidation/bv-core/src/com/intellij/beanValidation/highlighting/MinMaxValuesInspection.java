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

package com.intellij.beanValidation.highlighting;

import com.intellij.beanValidation.constants.BvAnnoConstants;
import com.intellij.beanValidation.highlighting.fixes.SwapMinMax;
import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiConstantEvaluationHelperImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public class MinMaxValuesInspection extends BaseBeanValidationInspection {
  private static final PsiConstantEvaluationHelper HELPER = new PsiConstantEvaluationHelperImpl();

  @Nls
  @NotNull
  public String getDisplayName() {
    return BVInspectionBundle.message("min.max.inspection");
  }

  @Override
  public ProblemDescriptor[] checkAnnotation(@NotNull PsiAnnotation anno, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (isMinMaxAnnotation(anno)) {
      final PsiAnnotationMemberValue maxValue = anno.findAttributeValue("max");
      final PsiAnnotationMemberValue minValue = anno.findAttributeValue("min");
      if (maxValue == null || minValue == null) return null;

      final PsiElement maxElement = maxValue.getOriginalElement();
      final PsiElement minElement = minValue.getOriginalElement();
      if (maxElement instanceof PsiExpression && minElement instanceof PsiExpression) {
        final Integer max = getIntOrNull(HELPER.computeConstantExpression((PsiExpression)maxElement));
        final Integer min = getIntOrNull(HELPER.computeConstantExpression((PsiExpression)minElement));
        if (max == null && min == null) return null;
        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();

        if (min != null && min.intValue() < 0 && isLengthAnnotation(anno)) {
          problems.add(
            manager.createProblemDescriptor(minElement, BVInspectionBundle.message("min.value.is.negative"),
                                            LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
        }

        if (max != null && min != null) {
          if (min > max) {
            problems.add(
              manager.createProblemDescriptor(maxElement, BVInspectionBundle.message("max.value.is.less.than.min"),
                                              new SwapMinMax(minElement, maxElement), ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
          }
        }

        return problems.toArray(new ProblemDescriptor[problems.size()]); 
      }
    }
    return null;
  }

  @Nullable
  public static Integer getIntOrNull(@Nullable Object obj) {
    final String value = (obj == null) ? null : obj.toString();
    if (value == null) return null;
    try {
      return new Integer(value);
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean isMinMaxAnnotation(@NotNull PsiAnnotation anno) {
    return isLengthAnnotation(anno) || isSizeAnnotation(anno);
  }

  public static boolean isLengthAnnotation(@NotNull PsiAnnotation anno) {
    return BvAnnoConstants.LENGTH.equals(anno.getQualifiedName());
  }

  public static boolean isSizeAnnotation(@NotNull PsiAnnotation anno) {
    return BvAnnoConstants.SIZE.equals(anno.getQualifiedName());
  }
}
