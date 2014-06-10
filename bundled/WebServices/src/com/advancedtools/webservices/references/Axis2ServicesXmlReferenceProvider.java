package com.advancedtools.webservices.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.AndFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.TextFilter;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class Axis2ServicesXmlReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String PARAMETER_TAG_NAME = "parameter";
  public static final @NonNls String MESSAGE_RECEIVER_TAG_NAME = "messageReceiver";
  public static final @NonNls String CLASS_ATTR_NAME = "class";
  
  private final MyReferenceProvider myClassProvider;
  @NonNls
  public static final String NAME_ATTR_NAME = "name";
  @NonNls
  public static final String OPERATION_TAG_NAME = "operation";
  @NonNls
  private static final String SERVICE_CLASS_ATTR_VALUE = "ServiceClass";

  public Axis2ServicesXmlReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
    //myPathProvider = registry.getProviderByType(ReferenceProvidersRegistry.PATH_REFERENCES_PROVIDER);
  }

  public ElementFilter getTagFilter() {
    return new TextFilter(
      PARAMETER_TAG_NAME
    );
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new AndFilter(
        new TextFilter(MESSAGE_RECEIVER_TAG_NAME, OPERATION_TAG_NAME),
        new NamespaceFilter("") // no ns
      ),
      2
    );
  }

  public String[] getTagCandidateNames() {
    return new String[] { PARAMETER_TAG_NAME };
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      CLASS_ATTR_NAME, NAME_ATTR_NAME
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    PsiReference[] result = PsiReference.EMPTY_ARRAY;

    if (element instanceof XmlAttributeValue) {
      final XmlAttribute xmlAttribute = (XmlAttribute) element.getParent();
      final String attrName = xmlAttribute.getName();
      final String tagName = xmlAttribute.getParent().getName();

      if (CLASS_ATTR_NAME.equals(attrName) && MESSAGE_RECEIVER_TAG_NAME.equals(tagName)) {
        result = myClassProvider.getReferencesByElement(element);
      } else if (NAME_ATTR_NAME.equals(attrName) && OPERATION_TAG_NAME.equals(tagName)) {
        result = new PsiReference[] {
          new WSDDReferenceProvider.WSMethodReference(element, new TextRange(1,element.getTextLength() - 1)) {

            protected PsiReference findReference(XmlTag p) {
              if (SERVICE_CLASS_ATTR_VALUE.equals(p.getAttributeValue(NAME_ATTR_NAME))) {
                final PsiReference[] references = p.getReferences();
                return references[references.length - 1];
              }
              return null;
            }
          }
        };
      }

    } else if (element instanceof XmlTag) {
      final XmlTag tag = (XmlTag) element;

      if (PARAMETER_TAG_NAME.equals(tag.getName()) && SERVICE_CLASS_ATTR_VALUE.equals(tag.getAttributeValue(NAME_ATTR_NAME))) {
        PsiReference[] referencesByElement = myClassProvider.getReferencesByElement(element);
        result = new PsiReference[referencesByElement.length + 1];
        System.arraycopy(referencesByElement, 0 , result, 0, referencesByElement.length);

        result[referencesByElement.length] = new TagValueClassReference(tag);
      }
    }

    return result;
  }

}
