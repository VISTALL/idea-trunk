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

package com.intellij.hibernate.model.annotations.mapping;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.converters.PropertyTypeResolvingConverter;
import com.intellij.jam.JamSimpleReferenceConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.jpa.model.annotations.mapping.AttributeBaseImpl;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class JpaJamExtender {

  public static final JamStringAttributeMeta.Single<PsiType> TYPE_ANNO_TYPE_VALUE_META = JamAttributeMeta.singleString(HibernateConstants.TYPE_PARAM, new JamSimpleReferenceConverter<PsiType>() {

    @Override
    protected PsiElement getPsiElementFor(@NotNull PsiType target) {
      return PropertyTypeResolvingConverter.getPsiElementInner(target);
    }

    @Override
    public PsiType fromString(@Nullable String s, JamStringAttributeElement<PsiType> context) {
      return PropertyTypeResolvingConverter.fromStringInner(s, context.getPsiManager().getProject(), null);
    }

    @Override
    public LookupElement[] getLookupVariants(JamStringAttributeElement<PsiType> context) {
      final PsiFile file = context.getPsiElement().getContainingFile();
      final PsiMember element = PsiTreeUtil.getParentOfType(context.getPsiElement(), PsiMember.class);
      final PsiMember original = element == null? null : PsiUtil.getOriginalElement(element, PsiMember.class);
      // todo add mapping to collect typedefs
      return PropertyTypeResolvingConverter.getVariantsInner(AttributeBaseImpl.getAttribute(original),
                                                             file, null, ModuleUtil.findModuleForPsiElement(file));
    }

    @Override
    public PsiElement bindReference(JamStringAttributeElement<PsiType> context, PsiElement element) {
      if (element instanceof PsiClass) {
        context.setStringValue(((PsiClass)element).getQualifiedName());
        return context.getPsiElement();
      }
      else return super.bindReference(context, element);
    }
  });

  public static final JamAnnotationMeta TYPE_ANNO_META = new JamAnnotationMeta(HibernateConstants.TYPE_ANNO).addAttribute(TYPE_ANNO_TYPE_VALUE_META);

  private JpaJamExtender() {
  }

  public static void extendJpa() {
    AttributeBaseImpl.ATTRIBUTE_ARCHETYPE.addAnnotation(TYPE_ANNO_META);

  }
}
