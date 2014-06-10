package com.advancedtools.webservices.utils.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public class WebServicesFacet extends Facet<WebServicesFacetConfiguration> {
  public static final FacetTypeId<WebServicesFacet> ID = new FacetTypeId<WebServicesFacet>("WebServices");
  public static final FacetType<WebServicesFacet, WebServicesFacetConfiguration> ourFacetType = new WebServicesFacetType();

  public WebServicesFacet(@NotNull Module module, String name,
                          @NotNull WebServicesFacetConfiguration webServicesFacetConfiguration,
                          Facet underlyingFacet
                          ) {
    super(ourFacetType, module, name, webServicesFacetConfiguration, underlyingFacet);
  }
}
