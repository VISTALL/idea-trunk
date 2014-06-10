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
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Konstantin Bulenkov
 */
public class BvFieldNameConverter extends ResolvingConverter<PsiField> implements CustomReferenceConverter<PsiField> {

  @NotNull
  @Override
  public Collection<? extends PsiField> getVariants(ConvertContext context) {
    PsiClass psiClass = BVUtils.getBeanClass(context);
    return psiClass == null ? Collections.<PsiField>emptyList() : Arrays.asList(psiClass.getFields());
  }

  @Override
  public PsiField fromString(@Nullable @NonNls String s, ConvertContext context) {
    if (s == null) return null;
    for (PsiField field : getVariants(context)) {
      if (s.equals(field.getName())) {
        return field;
      }
    }
    return null;
  }

  @Override
  public String toString(@Nullable PsiField psiField, ConvertContext context) {
    return psiField == null ? null : psiField.getName();
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<PsiField> domValue, PsiElement element, final ConvertContext context) {
    final PsiReferenceBase ref = new PsiReferenceBase<PsiElement>(element, true) {
      public PsiElement resolve() {
        return domValue.getValue();
      }

      @NotNull
      public Object[] getVariants() {
        return BvFieldNameConverter.this.getVariants(context).toArray();
      }

      @Override
      public boolean isReferenceTo(PsiElement element) {
        return super.isReferenceTo(element);
      }
    };
    return new PsiReference[]{ref};
  }
}
