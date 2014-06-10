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

package com.intellij.beanValidation.highlighting.checkers;

import com.intellij.beanValidation.highlighting.fixes.CreateConstraintParamsFix;
import com.intellij.beanValidation.model.xml.Constraint;
import com.intellij.beanValidation.model.xml.Element;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiAnnotationMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class CheckMissingParameters implements BvChecker {
  public void check(GenericDomValue value, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
    final Object o = value.getValue();
    final DomElement element = value.getParent();
    if (o instanceof PsiClass && ((PsiClass)o).isAnnotationType() && element instanceof Constraint) {
      final Constraint constraint = (Constraint)element;
      final PsiClass psiClass = (PsiClass)o;
      final List<PsiMethod> methods = new ArrayList<PsiMethod>();
      for (PsiMethod method : psiClass.getMethods()) {
        if (method instanceof PsiAnnotationMethod && ((PsiAnnotationMethod)method).getDefaultValue() == null) {
          methods.add(method);
        }
      }

      if (methods.isEmpty()) return;

      for (Element elem : constraint.getElements()) {
        methods.remove(elem.getName().getValue());        
      }
      if (methods.size() != 0) {
        holder.createProblem(constraint,
                             HighlightSeverity.ERROR,
                             BVBundle.message("annotation.params.missed"),
                             new CreateConstraintParamsFix(constraint, methods));
      }
    }
  }
}
