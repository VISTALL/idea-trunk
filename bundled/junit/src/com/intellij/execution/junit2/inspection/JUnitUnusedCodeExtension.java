/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

/*
 * User: anna
 * Date: 28-May-2007
 */
package com.intellij.execution.junit2.inspection;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.deadCode.UnusedCodeExtension;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runners.Parameterized;

import java.util.Arrays;

public class JUnitUnusedCodeExtension extends UnusedCodeExtension {
  public boolean ADD_JUNIT_TO_ENTRIES = true;

  @NotNull
  public String getDisplayName() {
    return InspectionsBundle.message("inspection.dead.code.option2");
  }

  public boolean isEntryPoint(RefElement refElement) {
    return isEntryPoint(refElement.getElement());
  }

  @Override
  public boolean isEntryPoint(PsiElement psiElement) {
    if (ADD_JUNIT_TO_ENTRIES) {
      if (psiElement instanceof PsiClass) {
        final PsiClass aClass = (PsiClass)psiElement;
        if (JUnitUtil.isTestClass(aClass)) {
          return true;
        }
      }
      else if (psiElement instanceof PsiMethod) {
        final PsiMethod psiMethod = (PsiMethod)psiElement;
        final String name = psiMethod.getName();
        if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC) &&
             !psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
          if (name.startsWith("test") || "suite".equals(name) || "setUp".equals(name) || "tearDown".equals(name)) {
            return true;
          }
          if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
            if (AnnotationUtil.isAnnotated(psiMethod, Arrays.asList(BeforeClass.class.getName(), AfterClass.class.getName(), Parameterized.Parameters.class.getName().replace('$', '.')))) return true;
          } else {
            if (AnnotationUtil.isAnnotated(psiMethod, Arrays.asList(Before.class.getName(), After.class.getName(), Test.class.getName()))) return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isSelected() {
    return ADD_JUNIT_TO_ENTRIES;
  }

  public void setSelected(boolean selected) {
    ADD_JUNIT_TO_ENTRIES = selected;
  }

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }
}