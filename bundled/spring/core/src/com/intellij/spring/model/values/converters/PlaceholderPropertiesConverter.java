package com.intellij.spring.model.values.converters;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.values.PlaceholderUtils;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderPropertiesConverter extends Converter<String> implements CustomReferenceConverter {

  public String fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return s;
  }

  public String toString(@Nullable String s, final ConvertContext context) {
    return s;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    return PlaceholderUtils.createPlaceholderPropertiesReferences(genericDomValue);
  }

  public static class PlaceholderPropertiesCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    public boolean value(final Pair<PsiType, GenericDomValue> pair) {
      return PlaceholderUtils.isPlaceholder(pair.getSecond());
    }
  }

}
