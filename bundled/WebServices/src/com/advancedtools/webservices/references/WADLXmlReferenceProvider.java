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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @by maxim
 */
public class WADLXmlReferenceProvider extends MyReferenceProvider {
  public static final @NonNls String HREF_ATTRIBUTE_NAME = "href";
  public static final @NonNls String OUR_NS = "http://research.sun.com/wadl/2006/10";
  //public static final String OUR_NS2 = "http://research.sun.com/wadl";
  private final MyReferenceProvider  pathReferenceProvider;

  public WADLXmlReferenceProvider(Project project) {
    pathReferenceProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, false);
  }

  public ElementFilter getAttributeFilter() {
    return new ParentElementFilter(
      new AndFilter(
        new TextFilter("include"),
        new NamespaceFilter(OUR_NS)
    ), 2);
  }

  public String[] getAttributeCandidateNames() {
    return new String[] {
      HREF_ATTRIBUTE_NAME
    };
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    return pathReferenceProvider.getReferencesByElement(element);
  }

}
