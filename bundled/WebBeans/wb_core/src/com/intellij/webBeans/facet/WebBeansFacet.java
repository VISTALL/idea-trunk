package com.intellij.webBeans.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

public class WebBeansFacet extends Facet<WebBeansFacetConfiguration> {
  public final static FacetTypeId<WebBeansFacet> FACET_TYPE_ID = new FacetTypeId<WebBeansFacet>("WebBeans");

  public WebBeansFacet(final FacetType facetType, final Module module, final String name, final WebBeansFacetConfiguration configuration, final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  @Nullable
  public static WebBeansFacet getInstance(Module module) {
    return FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID);
  }
}
