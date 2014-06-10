package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Maxim
 */
public class WebServicesClientFacetType extends FacetType<WebServicesClientFacet, WebServicesClientFacetConfiguration> {
  public WebServicesClientFacetType() {
    super(WebServicesClientFacet.ID, "WebServicesClient", WSBundle.message("webservicesclient.facet.name"));
  }

  public WebServicesClientFacetConfiguration createDefaultConfiguration() {
    return new WebServicesClientFacetConfiguration();
  }

  public WebServicesClientFacet createFacet(@NotNull Module module, String s, @NotNull WebServicesClientFacetConfiguration webServicesClientFacetConfiguration, @Nullable Facet facet) {
    return new WebServicesClientFacet(module, s, webServicesClientFacetConfiguration, facet);
  }

  public Icon getIcon() {
    return IconLoader.findIcon("/javaee/WebServiceClient.png");
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return EnvironmentFacade.getInstance().isJavaModuleType(moduleType);
  }
}
