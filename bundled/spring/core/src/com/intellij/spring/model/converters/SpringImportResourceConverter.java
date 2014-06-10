package com.intellij.spring.model.converters;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.*;
import com.intellij.spring.model.values.PlaceholderUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class SpringImportResourceConverter extends Converter<PsiFile> implements CustomReferenceConverter {

  public PsiFile fromString(final @Nullable String s, final ConvertContext context) {
    if (s != null) {
      final GenericAttributeValue<PsiFile> element = (GenericAttributeValue<PsiFile>)context.getInvocationElement();

      if (!s.contains(PlaceholderUtils.DEFAULT_PLACEHOLDER_PREFIX)) {
        final PsiReference[] references = createReferences(element, element.getXmlAttributeValue(), context);
        if (references.length > 0) {
          PsiElement result = references[references.length - 1].resolve();
          if (result instanceof PsiFile) {
            return (PsiFile)result;
          }
        }
      }
    }
    return null;
  }

  public String toString(final @Nullable PsiFile psiFile, final ConvertContext context) {
    return null;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final String s = genericDomValue.getStringValue();
    if (s == null || element == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (s.contains(PlaceholderUtils.DEFAULT_PLACEHOLDER_PREFIX)) {
      return PlaceholderUtils.createPlaceholderPropertiesReferences(genericDomValue);
    }

    return ResourceResolverUtils.getReferences(element, s, s.startsWith("/"), false);
  }

}
