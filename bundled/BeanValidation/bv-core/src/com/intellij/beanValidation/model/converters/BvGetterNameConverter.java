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

import com.intellij.beanValidation.utils.BVUtils;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public class BvGetterNameConverter extends ResolvingConverter<PsiMethod> {//implements CustomReferenceConverter<PsiMethod> {

  @NotNull
  @Override
  public Collection<? extends PsiMethod> getVariants(ConvertContext context) {
    PsiClass psiClass = BVUtils.getBeanClass(context);
    return psiClass == null ? Collections.<PsiMethod>emptyList() : getAllGetters(psiClass);
  }

  private static Collection<PsiMethod> getAllGetters(PsiClass psiClass) {
    final List<PsiMethod> getters = new ArrayList<PsiMethod>();
    for (PsiMethod method : psiClass.getMethods()) {
      if (PropertyUtil.isSimplePropertyGetter(method)) {
        getters.add(method);
      }
    }
    return getters;
  }

  @Override
  public PsiMethod fromString(@Nullable @NonNls String s, ConvertContext context) {
    if (s == null) return null;
    for (PsiMethod method : getVariants(context)) {
      if (s.equals(PropertyUtil.getPropertyName(method))) {
        return method;
      }
    }
    return null;
  }

  @Override
  public String toString(@Nullable PsiMethod method, ConvertContext context) {
    return method == null ? null : PropertyUtil.getPropertyName(method);
  }

  @Override
  public void handleElementRename(GenericDomValue<PsiMethod> genericValue, ConvertContext context, String newElementName) {
    super.handleElementRename(genericValue, context, PropertyUtil.getPropertyName(newElementName));
  }
}
