package com.advancedtools.webservices.utils.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public class WebServicesClientFacet extends Facet<WebServicesClientFacetConfiguration> {
  public static final FacetTypeId<WebServicesClientFacet> ID = new FacetTypeId<WebServicesClientFacet>("WebServicesClient");
  public static final WebServicesClientFacetType ourFacetType = new WebServicesClientFacetType();

  public WebServicesClientFacet(@NotNull Module module, String name,
                          @NotNull WebServicesClientFacetConfiguration webServicesFacetConfiguration,
                          Facet underlyingFacet
                          ) {
    super(ourFacetType, module, name, webServicesFacetConfiguration, underlyingFacet);
  }
}