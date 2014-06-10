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
public class JBossWSXmlReferenceProvider extends MyReferenceProvider {
  private final MyReferenceProvider myClassProvider;
  private final MyReferenceProvider myPathProvider;

  @NonNls
  public static final String HTTP_WWW_JBOSS_ORG_JBOSSWS_TOOLS = "http://www.jboss.org/jbossws-tools";
  @NonNls private static final String ENDPOINT_ATTR_NAME = "endpoint";
  @NonNls private static final String FILE_ATTR_NAME = "file";

  public JBossWSXmlReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, false);
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new AndFilter(
        new TextFilter("service", "mapping"),
        new NamespaceFilter(HTTP_WWW_JBOSS_ORG_JBOSSWS_TOOLS)
    ), 2);
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      ENDPOINT_ATTR_NAME,
      FILE_ATTR_NAME
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    final String name = ((XmlAttribute) element.getParent()).getName();

    if (FILE_ATTR_NAME.equals(name)) {
      return myPathProvider != null ? myPathProvider.getReferencesByElement(element): PsiReference.EMPTY_ARRAY;
    }
    return myClassProvider.getReferencesByElement(element);
  }

}