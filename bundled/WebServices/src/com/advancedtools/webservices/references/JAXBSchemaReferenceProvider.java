package com.advancedtools.webservices.references;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author maxim
 */
public class JAXBSchemaReferenceProvider extends MyReferenceProvider {
  public ElementFilter getFilter() {
    return null;
  }

  static class JaxbClassReference extends WSDLReferenceProvider.WsdlClassReference {
    public JaxbClassReference(PsiElement psiElement, int index, int endIndex) {
      super(psiElement, index, endIndex);
    }

    public String getCanonicalText() {
      return StringUtil.capitalize(super.getCanonicalText());
    }
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    if (!(element instanceof XmlAttributeValue)) return PsiReference.EMPTY_ARRAY;
    XmlTag tag = (XmlTag) element.getParent().getParent();
    String tagLocalName = tag.getLocalName();
    String tagNS = tag.getNamespace();

    if (Arrays.binarySearch(WSDLReferenceProvider.SCHEMA_URIS,tagNS) >= 0) {
      PsiFile psiFile = tag.getContainingFile().getOriginalFile();
      VirtualFile file = psiFile != null ? psiFile.getVirtualFile():null;

      if (file == null ||
        !WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(file.getExtension())) {
        return PsiReference.EMPTY_ARRAY;
      }

      if (WSDLReferenceProvider.COMPLEX_TYPE_TAG_NAME.equals(tagLocalName)) {
        return new PsiReference[] {
          new JaxbClassReference(element, 1, element.getTextLength() - 1)
        };
      } else if (WSDLReferenceProvider.ELEMENT_TAG_NAME.equals(tagLocalName)) {
        return new PsiReference[] {
          new WSDLReferenceProvider.WsdlPropertyReference(element)
        };
      }

    }
    return PsiReference.EMPTY_ARRAY;
  }

  public String[] getAttributeNames() { return new String[] { WSDLReferenceProvider.NAME_ATTR_NAME}; }
}
