package com.advancedtools.webservices.references;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.AndFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.XmlTagFilter;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class JaxRPCMappingReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String JAVA_TYPE_TAG_NAME = "java-type";
  public static final @NonNls String PARAM_TYPE_TAG_NAME = "param-type";
  public static final @NonNls String METHOD_RETURN_TYPE_TAG_NAME = "method-return-type";
  public static final @NonNls String JAVA_VARIABLE_NAME_TAG_NAME = "java-variable-name";
  public static final @NonNls String JAVA_METHOD_NAME_TAG_NAME = "java-method-name";
  public static final @NonNls String SERVICE_ENDPOINT_INTERFACE_TAG_NAME = "service-endpoint-interface";
  public static final @NonNls String SERVICE_INTERFACE_TAG_NAME = "service-interface";

  private static final @NonNls String WSDL_FILE_TAG_NAME = "wsdl-file";
  private static final @NonNls String JAXRPC_MAPPING_FILE_TAG_NAME = "jaxrpc-mapping-file";
  private final MyReferenceProvider myPathProvider;

  public JaxRPCMappingReferenceProvider(Project project) {
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, true);
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
    final XmlTag parentTag = (XmlTag) psiElement;
    final String localName = parentTag.getLocalName();

    if (JAVA_METHOD_NAME_TAG_NAME.equals(localName)) {
      final TextRange valueRange = parentTag.getValue().getTextRange();
      final TextRange tagRange = parentTag.getTextRange();

      return new PsiReference[] {
        new WSDLReferenceProvider.WsdlMethodReference(
          parentTag,
          valueRange.getStartOffset() - tagRange.getStartOffset(),
          valueRange.getEndOffset() - tagRange.getStartOffset()
        ) {
          protected PsiElement resolveClass() {
            return findClassFromGrandGrandParentTag(parentTag, SERVICE_ENDPOINT_INTERFACE_TAG_NAME);
          }
          public boolean isSoft() {
            return false;
          }
        }
      };
    } else if (JAVA_VARIABLE_NAME_TAG_NAME.equals(localName)) {
      final TextRange valueRange = parentTag.getValue().getTextRange();
      final TextRange tagRange = parentTag.getTextRange();

      return new PsiReference[] {
        new WSDLReferenceProvider.WsdlPropertyReference(
          parentTag,
          valueRange.getStartOffset() - tagRange.getStartOffset(),
          valueRange.getEndOffset() - tagRange.getStartOffset()
        ) {
          protected PsiClass resolveClass() {
            return findClassFromGrandGrandParentTag(parentTag, JAVA_TYPE_TAG_NAME);
          }

          public boolean isSoft() {
            return false;
          }
        }
      };
    } else if (SERVICE_ENDPOINT_INTERFACE_TAG_NAME.equals(localName) ||
               SERVICE_INTERFACE_TAG_NAME.equals(localName) ||
               METHOD_RETURN_TYPE_TAG_NAME.equals(localName) ||
               JAVA_TYPE_TAG_NAME.equals(localName) ||
               PARAM_TYPE_TAG_NAME.equals(localName)
              ) {
      return new PsiReference[] {new TagValueClassReference(parentTag) };
    } else if (JAXRPC_MAPPING_FILE_TAG_NAME.equals(localName) ||
               WSDL_FILE_TAG_NAME.equals(localName)
              ) {
      return myPathProvider.getReferencesByElement(parentTag);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiClass findClassFromGrandGrandParentTag(XmlTag parentTag, String tagName) {
    final XmlTag grandParentTag = parentTag.getParentTag();

    if (grandParentTag != null) {
      final XmlTag grandGrandParentTag = grandParentTag.getParentTag();

      if (grandGrandParentTag != null) {
        final XmlTag firstSubTag = grandGrandParentTag.findFirstSubTag(tagName);

        if (firstSubTag != null) {
          return findFirstResolvedClassFromReferences(firstSubTag);
        }
      }
    }

    return null;
  }

  private static PsiClass findFirstResolvedClassFromReferences(XmlTag firstSubTag) {
    final PsiReference[] references = firstSubTag.getReferences();

    for(int i = 2; i < references.length; ++i) {
      final PsiElement psiElement = references[i].resolve();
      if (psiElement instanceof PsiClass) return (PsiClass) psiElement;
    }
    return null;
  }

  public String[] getTagCandidateNames() {
    return new String[] {
      JAVA_TYPE_TAG_NAME,
      PARAM_TYPE_TAG_NAME,
      METHOD_RETURN_TYPE_TAG_NAME,
      JAVA_VARIABLE_NAME_TAG_NAME,
      JAVA_METHOD_NAME_TAG_NAME,
      SERVICE_ENDPOINT_INTERFACE_TAG_NAME,
      SERVICE_INTERFACE_TAG_NAME,
      WSDL_FILE_TAG_NAME,
      JAXRPC_MAPPING_FILE_TAG_NAME
    };
  }

  public ElementFilter getTagFilter() {
    return new AndFilter(
      new ParentElementFilter(XmlTagFilter.INSTANCE, 1),
      new NamespaceFilter(new String[] {"http://java.sun.com/xml/ns/j2ee","http://java.sun.com/xml/ns/javaee"})
    );
  }
}
