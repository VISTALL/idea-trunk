package com.advancedtools.webservices.references;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class CxfXmlReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String NS = "http://cxf.apache.org/jaxws";
  private final MyReferenceProvider myClassProvider;
  @NonNls
  private static final String IMPLEMENTOR_ATTR_NAME = "implementor";
  @NonNls
  private static final String WSDL_LOCATION_ATTR_NAME = "wsdlLocation";
  private final MyReferenceProvider myPathProvider;

  public CxfXmlReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project,true);
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new NamespaceFilter(NS),
      2
    );
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      IMPLEMENTOR_ATTR_NAME,
      WSDL_LOCATION_ATTR_NAME
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    final PsiElement parent = element.getParent();
    if (parent instanceof XmlAttribute) {
      if (IMPLEMENTOR_ATTR_NAME.equals(((XmlAttribute)parent).getName())) {
        PsiReference[] references = myClassProvider.getReferencesByElement(element);
        if (references.length > 0) return updateLastClassRefWithAnyMemberRef(references);
      }
      return myPathProvider.getReferencesByElement(element);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  public static PsiReference[] updateLastClassRefWithAnyMemberRef(PsiReference[] references) {
    final PsiReference[] result = new PsiReference[references.length];
    System.arraycopy(references, 0, result, 0, references.length);
    references = result;
    references[references.length - 1] = new DelegatingClassReferenceThatReferencesAnyMethod(references[references.length - 1]);

    return references;
  }

}
