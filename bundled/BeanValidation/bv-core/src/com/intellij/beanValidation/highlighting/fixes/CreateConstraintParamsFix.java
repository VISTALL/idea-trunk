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

import com.intellij.beanValidation.model.xml.Constraint;
import com.intellij.beanValidation.model.xml.Element;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class CreateConstraintParamsFix extends BaseBVQuickFix{
  private final Constraint myConstraint;
  private final List<PsiMethod> myMethods;

  public CreateConstraintParamsFix(Constraint constraint, List<PsiMethod> methods) {
    super(BVBundle.message("create.missing.elements"));
    myConstraint = constraint;
    myMethods = methods;
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    for (PsiMethod method : myMethods) {
      final Element element = myConstraint.addElement();
      element.getName().setValue(method);
      final String defaultValue = PsiTypesUtil.getDefaultValueOfType(method.getReturnType());
      element.setValue("null".equals(defaultValue) ? "value" : defaultValue);
    }
  }
}
