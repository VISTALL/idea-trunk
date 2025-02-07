/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.devkit.inspections.quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.DevKitBundle;

/**
 * @author swr
 */
public class CreateConstructorFix extends BaseFix {

  public CreateConstructorFix(PsiClass checkedClass, boolean onTheFly) {
    super(checkedClass, onTheFly);
  }

  @NotNull
  public String getName() {
    return DevKitBundle.message("inspections.registration.problems.quickfix.create.constructor");
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  protected void doFix(Project project, ProblemDescriptor descriptor, boolean external) throws IncorrectOperationException {
    final PsiClass clazz = (PsiClass)myElement;

    PsiMethod ctor = JavaPsiFacade.getInstance(clazz.getProject()).getElementFactory().createConstructor();
    PsiUtil.setModifierProperty(ctor, PsiModifier.PUBLIC, true);

    final PsiMethod[] constructors = clazz.getConstructors();
    if (constructors.length > 0) {
      ctor = (PsiMethod)clazz.addBefore(ctor, constructors[0]);
    } else {
      // shouldn't get here - it's legal if there's no ctor present at all
      ctor = (PsiMethod)clazz.add(ctor);
    }

    if (myOnTheFly) ctor.navigate(true);
  }
}
