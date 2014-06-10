package com.advancedtools.webservices.references;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.AndFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.TextFilter;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class XFireServicesXmlReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String SERVICE_CLASS_TAG_NAME = "serviceClass";
  public static final @NonNls String IMPLEMENTATION_CLASS_TAG_NAME = "implementationClass";
  public static final @NonNls String SERVICE_FACTTORY_CLASS_TAG_NAME = "serviceFactory";
  public static final @NonNls String HTTP_XFIRE_CODEHAUS_ORG_CONFIG_1_0 = "http://xfire.codehaus.org/config/1.0";
  private final MyReferenceProvider myClassProvider;
  private final MyPathReferenceProvider myPathProvider;
  private static final @NonNls String WSDL_URL_TAG_NAME = "wsdlURL";

  public XFireServicesXmlReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, false);
  }

  public ElementFilter getTagFilter() {
    return new AndFilter(
      new ElementFilter[] {
        new TextFilter(
          new String[] {
            SERVICE_CLASS_TAG_NAME,
            IMPLEMENTATION_CLASS_TAG_NAME,
            SERVICE_FACTTORY_CLASS_TAG_NAME,
            WSDL_URL_TAG_NAME
          }
        ),
        new NamespaceFilter(HTTP_XFIRE_CODEHAUS_ORG_CONFIG_1_0)
      }
    );
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new NamespaceFilter(HTTP_XFIRE_CODEHAUS_ORG_CONFIG_1_0),
      2
    );
  }

  public String[] getTagCandidateNames() {
    return new String[] {
      SERVICE_CLASS_TAG_NAME,
      IMPLEMENTATION_CLASS_TAG_NAME,
      SERVICE_FACTTORY_CLASS_TAG_NAME
    };
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      "handlerClass"
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    if (element instanceof XmlTag) {
      final XmlTag tag = ((XmlTag)element);
      final String localName = ((XmlTag)element).getLocalName();

      if (WSDL_URL_TAG_NAME.equals(localName)) {
        final String text = tag.getValue().getText();
        String trimmedText = text.trim();

        int offset = tag.getValue().getTextRange().getStartOffset() - tag.getTextRange().getStartOffset() + text.indexOf(trimmedText);
        final String prefix = "file://";
        if (trimmedText.startsWith(prefix)) {
          trimmedText = trimmedText.substring(prefix.length());
          offset += prefix.length();
        }
        return myPathProvider.getReferencesByString(trimmedText, tag, offset);
      }

      PsiReference[] references = myClassProvider.getReferencesByElement(element);
      if (references.length > 0 && (SERVICE_CLASS_TAG_NAME.equals(localName) || IMPLEMENTATION_CLASS_TAG_NAME.equals(localName))) {
        references = CxfXmlReferenceProvider.updateLastClassRefWithAnyMemberRef(references);
      }
      return references;
    }

    return myClassProvider.getReferencesByElement(element);
  }

}
