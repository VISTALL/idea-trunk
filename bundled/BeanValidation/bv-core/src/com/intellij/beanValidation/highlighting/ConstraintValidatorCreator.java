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
import com.intellij.beanValidation.highlighting.fixes.CreateConstraintValidatorFix;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class ConstraintValidatorCreator extends BaseBeanValidationInspection {
  @Override
  public ProblemDescriptor[] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
    final PsiModifierList modifierList = aClass.getModifierList();
    if (modifierList == null) return null;

    final PsiAnnotation anno = modifierList.findAnnotation(BvAnnoConstants.CONSTRAINT);
    if (anno == null) return null;

    PsiElement value = anno.findAttributeValue(BvAnnoConstants.VALIDATED_BY);
    if (value instanceof PsiClassObjectAccessExpression) {
      value = ((PsiClassObjectAccessExpression)value).getOperand().getFirstChild();
    }
    if (value instanceof PsiJavaCodeReferenceElement && ((PsiJavaCodeReferenceElement)value).resolve() == null) {
      return new ProblemDescriptor[]{
        manager.createProblemDescriptor(value, BVInspectionBundle.message("constraint.validator.does.not.exist"),
                                        new CreateConstraintValidatorFix((PsiJavaCodeReferenceElement)value, aClass), ProblemHighlightType.ERROR)};

    }

    return super.checkClass(aClass, manager, isOnTheFly);
  }



  @Nls
  @NotNull
  public String getDisplayName() {
    return BVBundle.message("constraint.validator.creator");
  }
}
