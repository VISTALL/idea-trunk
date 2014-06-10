package com.intellij.seam.model.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class SeamEventTypeReferenceConverter implements CustomReferenceConverter<String> {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return new PsiReference[]{new SeamEventTypeReference<PsiElement>(element) {
      protected String getEventType(final PsiElement psiElement) {
        return genericDomValue.getStringValue();
      }
    }};
  }
}

