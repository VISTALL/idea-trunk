package com.intellij.seam.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.impl.autodetecting.FacetDetectorRegistryEx;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.constants.SeamConstants;
import com.intellij.seam.resources.SeamBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;

public class SeamFacetType extends FacetType<SeamFacet, SeamFacetConfiguration> {

  public final static SeamFacetType INSTANCE = new SeamFacetType();

  private SeamFacetType() {
    super(SeamFacet.FACET_TYPE_ID, "Seam", SeamBundle.message("seam.framework.name"));
  }

  public SeamFacetConfiguration createDefaultConfiguration() {
    return new SeamFacetConfiguration();
  }

  public SeamFacet createFacet(@NotNull final Module module, final String name, @NotNull final SeamFacetConfiguration configuration,
                                 final Facet underlyingFacet) {
    return new SeamFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public Icon getIcon() {
    return SeamIcons.SEAM_ICON;
  }

   public void registerDetectors(final FacetDetectorRegistry<SeamFacetConfiguration> facetDetectorRegistry) {
    FacetDetectorRegistryEx<SeamFacetConfiguration> registry = (FacetDetectorRegistryEx<SeamFacetConfiguration>)facetDetectorRegistry;
    registry.registerUniversalDetectorByFileNameAndRootTag(SeamConstants.SEAM_CONFIG_FILENAME, SeamConstants.SEAM_CONFIG_ROOT_TAG_NAME,
                                                           new SeamFacetDetector(), null);
  }

  private static class SeamFacetDetector extends FacetDetector<VirtualFile, SeamFacetConfiguration> {
    private SeamFacetDetector() {
      super("seam-detector");
    }

    public SeamFacetConfiguration detectFacet(final VirtualFile source, final Collection<SeamFacetConfiguration> existentFacetConfigurations) {
      Iterator<SeamFacetConfiguration> iterator = existentFacetConfigurations.iterator();
      if (iterator.hasNext()) {
        return iterator.next();
      }

      return new SeamFacetConfiguration();
    }
  }
}
