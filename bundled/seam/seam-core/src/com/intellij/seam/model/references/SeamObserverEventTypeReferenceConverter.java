package com.intellij.seam.model.references;

import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ConvertContext;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class SeamObserverEventTypeReferenceConverter implements CustomReferenceConverter<String> {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return new PsiReference[]{new SeamObserverEventTypeReference<PsiElement>(element) {
      protected String getEventType(final PsiElement psiElement) {
        return genericDomValue.getStringValue();
      }
    }};
  }
}


