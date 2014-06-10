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

package com.intellij.beanValidation.model.converters;

import com.intellij.beanValidation.model.xml.Constraint;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class AnnotationParameterConverter extends ResolvingConverter<PsiMethod> {
  @NotNull
  @Override
  public Collection<? extends PsiMethod> getVariants(ConvertContext context) {
    List<PsiMethod> names = new ArrayList<PsiMethod>();
    final Constraint constraint = context.getInvocationElement().getParentOfType(Constraint.class, true);
    if (constraint != null) {
      final PsiClass psiClass = constraint.getAnnotation().getValue();
      if (psiClass != null && psiClass.isAnnotationType()) {
        names.addAll(Arrays.asList(psiClass.getMethods()));
      }
    }
    return names;
  }

  @Override
  public PsiMethod fromString(@Nullable @NonNls String s, ConvertContext context) {
    final Constraint constraint = context.getInvocationElement().getParentOfType(Constraint.class, true);
    final PsiClass psiClass;
    if (constraint == null || s == null
        || (psiClass = constraint.getAnnotation().getValue()) == null
        || !psiClass.isAnnotationType()) return null;
    final PsiMethod[] methods = psiClass.findMethodsByName(s, true);
    if (methods.length == 1) {
      return methods[0];
    }
    return null;
  }

  @Override
  public String toString(@Nullable PsiMethod method, ConvertContext context) {
    return method == null ? null : method.getName();
  }
}
