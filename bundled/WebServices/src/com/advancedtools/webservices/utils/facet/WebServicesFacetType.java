package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Maxim
 */
public class WebServicesFacetType extends FacetType<WebServicesFacet, WebServicesFacetConfiguration> {
  public WebServicesFacetType() {
    super(WebServicesFacet.ID, "webservices", WSBundle.message("webservices.facet.name"), WebFacet.ID);
  }

  public WebServicesFacetConfiguration createDefaultConfiguration() {
    return new WebServicesFacetConfiguration();
  }

  public Icon getIcon() {
    return IconLoader.findIcon("/javaee/WebService.png");
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return EnvironmentFacade.getInstance().isJavaModuleType(moduleType);
  }

  public WebServicesFacet createFacet(@NotNull Module module, String s, @NotNull WebServicesFacetConfiguration webServicesFacetConfiguration, @Nullable Facet facet) {
    return new WebServicesFacet(module, s, webServicesFacetConfiguration, facet);
  }
}
