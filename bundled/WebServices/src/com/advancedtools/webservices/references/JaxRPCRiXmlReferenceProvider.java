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
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @by maxim
 */
public class JaxRPCRiXmlReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String OUR_NS = "http://java.sun.com/xml/ns/jax-rpc/ri/runtime";
  public static final @NonNls String SERVICE_ATTR_NAME = "interface";
  public static final @NonNls String TIE_ATTR_NAME = "tie";
  public static final @NonNls String IMPLEMENTATION_ATTR_NAME = "implementation";
  private static final @NonNls String MODEL_ATTR_NAME = "model";
  private static final @NonNls String WSDL_ATTR_NAME = "wsdl";
  private static final @NonNls String URL_PATTERN_ATTR_NAME = "urlpattern";
  private final MyReferenceProvider  myPathProvider;
  private final MyReferenceProvider myClassProvider;
  private final MyReferenceProvider myDynamicPathProvider;

  public JaxRPCRiXmlReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, true);
    myDynamicPathProvider = EnvironmentFacade.getInstance().acquireDynamicPathReferenceProvider(project);
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new AndFilter(
      new ElementFilter[] {
        new TextFilter("endpoint"),
        new NamespaceFilter(OUR_NS)
      }
    ), 2);
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      SERVICE_ATTR_NAME,
      IMPLEMENTATION_ATTR_NAME,
      TIE_ATTR_NAME,
      MODEL_ATTR_NAME,
      URL_PATTERN_ATTR_NAME,
      WSDL_ATTR_NAME
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    final String name = ((XmlAttribute) element.getParent()).getName();

    if (WSDL_ATTR_NAME.equals(name) || MODEL_ATTR_NAME.equals(name)) {
      return myPathProvider != null ? myPathProvider.getReferencesByElement(element): PsiReference.EMPTY_ARRAY;
    } else if (URL_PATTERN_ATTR_NAME.equals(name)) {
      return myDynamicPathProvider != null ? myDynamicPathProvider.getReferencesByElement(element) : PsiReference.EMPTY_ARRAY;
    }
    return myClassProvider.getReferencesByElement(element);
  }

}
