package com.intellij.webBeans.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.impl.autodetecting.FacetDetectorRegistryEx;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.constants.WebBeansCommonConstants;
import com.intellij.webBeans.resources.WebBeansBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;

public class WebBeansFacetType extends FacetType<WebBeansFacet, WebBeansFacetConfiguration> {

  public final static WebBeansFacetType INSTANCE = new WebBeansFacetType();

  private WebBeansFacetType() {
    super(WebBeansFacet.FACET_TYPE_ID, "WebBeans", WebBeansBundle.message("webBeans.framework.name"));
  }

  public WebBeansFacetConfiguration createDefaultConfiguration() {
    return new WebBeansFacetConfiguration();
  }

  public WebBeansFacet createFacet(@NotNull final Module module, final String name, @NotNull final WebBeansFacetConfiguration configuration,
                                 final Facet underlyingFacet) {
    return new WebBeansFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public Icon getIcon() {
    return WebBeansIcons.WEB_BEANS_ICON;
  }

   public void registerDetectors(final FacetDetectorRegistry<WebBeansFacetConfiguration> facetDetectorRegistry) {
    FacetDetectorRegistryEx<WebBeansFacetConfiguration> registry = (FacetDetectorRegistryEx<WebBeansFacetConfiguration>)facetDetectorRegistry;

    registry.registerUniversalDetectorByFileNameAndRootTag(WebBeansCommonConstants.WEB_BEANS_CONFIG_FILENAME, WebBeansCommonConstants.WEB_BEANS_CONFIG_ROOT_TAG_NAME,
                                                           new WebBeansFacetDetector(), null);
  }

  private static class WebBeansFacetDetector extends FacetDetector<VirtualFile, WebBeansFacetConfiguration> {
    private WebBeansFacetDetector() {
      super("webBeans-detector");
    }

    public WebBeansFacetConfiguration detectFacet(final VirtualFile source, final Collection<WebBeansFacetConfiguration> existentFacetConfigurations) {
      Iterator<WebBeansFacetConfiguration> iterator = existentFacetConfigurations.iterator();
      if (iterator.hasNext()) {
        return iterator.next();
      }

      return new WebBeansFacetConfiguration();
    }
  }
}
