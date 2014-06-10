/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class SpringBeanNamesConverter extends Converter<List<String>> implements CustomReferenceConverter<List<String>> {

  public String toString(final List<String> strings, final ConvertContext context) {
    return StringUtil.join(strings, ",");
  }

  public List<String> fromString(final String s, final ConvertContext context) {
    if (s == null) return null;

    return SpringUtils.tokenize(s);
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<List<String>> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    final List<String> strings = genericDomValue.getValue();
    if (strings != null) {
      List<PsiReference> references = new ArrayList<PsiReference>();
      for (String string : strings) {
        references.add(new PsiReferenceBase<PsiElement>(element, TextRange.from(element.getText().indexOf(string), string.length())) {

        public PsiElement resolve() {
          return getElement().getParent().getParent();
        }

        public boolean isSoft() {
          return true;
        }

        public Object[] getVariants() {
          return SpringUtils.suggestBeanNames(SpringConverterUtil.getCurrentBean(context));
        }

        public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
          final SpringBean bean = (SpringBean)genericDomValue.getParent();
          assert bean != null;
          bean.setName(newElementName);
          return element;
        }
      });
      }
      return references.toArray(new PsiReference[references.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }
}