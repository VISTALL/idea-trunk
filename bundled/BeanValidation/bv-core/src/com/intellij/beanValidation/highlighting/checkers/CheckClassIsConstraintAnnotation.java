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

import com.intellij.beanValidation.constants.BvAnnoConstants;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;

/**
 * @author Konstantin Bulenkov
 */
public class CheckClassIsConstraintAnnotation implements BvChecker {
  public void check(GenericDomValue value, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
    final Object psiClass = value.getValue();
    if (psiClass instanceof PsiClass) {
      final PsiClass anno = (PsiClass)psiClass;
      if (!anno.isAnnotationType()) {
        holder.createProblem(value, BVBundle.message("is.not.an.annotation", anno.getQualifiedName()));
      } else if (!AnnotationUtil.isAnnotated(anno, BvAnnoConstants.CONSTRAINT, true)) {
        holder.createProblem(value, BVBundle.message("is.not.a.constraint", anno.getQualifiedName()));
      }
    }
  }
}
