/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.values.converters;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.PairProcessor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

public class ResourceValueConverter extends Converter<Object> implements CustomReferenceConverter {

  public Object fromString(@Nullable @NonNls String s, final ConvertContext context) {
    final GenericDomValue domValue = (GenericDomValue)context.getInvocationElement();
    return StringUtil.isEmpty(s)
           ? Collections.emptySet()
           : ResourceResolverUtils.addResourceFilesFrom(domValue, s, new THashSet<PsiFileSystemItem>(), Condition.TRUE);
  }

  public String toString(@Nullable Object o, final ConvertContext context) {
    return null;
  }


  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final ArrayList<PsiReference> result = new ArrayList<PsiReference>();
    final int startInElement = ElementManipulators.getOffsetInElement(element);
    ResourceResolverUtils.processSeparatedString(genericDomValue.getStringValue(), ",", new PairProcessor<String, Integer>() {
      public boolean process(final String s, final Integer offset) {
        result.addAll(Arrays.asList(ResourceResolverUtils.getReferences(element, s, true, false, offset + startInElement, true)));
        return true;
      }
    });
    return result.isEmpty() ? PsiReference.EMPTY_ARRAY : result.toArray(new PsiReference[result.size()]);
  }

  public static class ResourceValueConverterCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    public boolean value(Pair<PsiType, GenericDomValue> pair) {
      PsiType psiType = pair.getFirst();
      if (psiType instanceof PsiArrayType) {
        psiType = ((PsiArrayType)psiType).getComponentType();
      }
      return psiType != null && "org.springframework.core.io.Resource".equals(psiType.getCanonicalText());
    }
  }
}