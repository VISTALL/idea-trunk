/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnusedDeclaration"})
public class AliasNameConverter implements CustomReferenceConverter<String> {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        return getElement().getParent().getParent();
      }

      public boolean isSoft() {
        return true;
      }

      public Object[] getVariants() {
        final Alias alias = genericDomValue.getParentOfType(Alias.class, false);

        if (alias != null) {
          final SpringBeanPointer beanPointer = alias.getAliasedBean().getValue();
          if (beanPointer != null) {
            return SpringUtils.suggestBeanNames(beanPointer.getSpringBean());
          }
        }
        return PsiReference.EMPTY_ARRAY;
      }
    }};
  }
}
