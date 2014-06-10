package com.sixrr.xrp.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class IdrefReferenceProvider extends PsiReferenceProvider {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull final ProcessingContext context) {
        if (!(psiElement instanceof XmlAttributeValue)) {
            return PsiReference.EMPTY_ARRAY;
        }
        XmlAttributeValue attributeValue = (XmlAttributeValue) psiElement;
        final XmlAttribute attribute = (XmlAttribute) attributeValue.getParent();
        if(!"idref".equalsIgnoreCase(attribute.getName()))
        {
            return PsiReference.EMPTY_ARRAY;
        }
        return new PsiReference[]{new IdrefReference(attributeValue)};
    }

}
