package com.intellij.hibernate.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import static com.intellij.patterns.StandardPatterns.string;
import com.intellij.util.descriptors.ConfigFileFactory;
import com.intellij.util.descriptors.ConfigFileMetaDataRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Gregory.Shrago
 */
public class HibernateFacetType extends FacetType<HibernateFacet, HibernateFacetConfiguration> {

  public static final HibernateFacetType INSTANCE = new HibernateFacetType();

  public HibernateFacetType() {
    super(HibernateFacet.ID, "hibernate", HibernateMessages.message("hibernate.facet.type.presentable.name"));
  }

  public HibernateFacetConfiguration createDefaultConfiguration() {
    final ConfigFileFactory factory = ConfigFileFactory.getInstance();
    final ConfigFileMetaDataRegistry metadataRegistry = factory.createMetaDataRegistry();
    metadataRegistry.registerMetaData(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA);
    return new HibernateFacetConfiguration(factory.createConfigFileInfoSet(metadataRegistry));
  }

  public HibernateFacet createFacet(@NotNull Module module, String name, @NotNull HibernateFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
    return new HibernateFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  @Nullable
  public Icon getIcon() {
    return HibernateIcons.HIBERNATE_ICON;
  }

  @Override
  public String getHelpTopic() {
    return "IntelliJ.IDEA.Procedures.Java.EE.Development.Managing.Facets.Facet.Specific.Settings.Hibernate";
  }

  public void registerDetectors(final FacetDetectorRegistry<HibernateFacetConfiguration> detectorRegistry) {
    FacetDetector<VirtualFile, HibernateFacetConfiguration> facetDetector = new FacetDetector<VirtualFile, HibernateFacetConfiguration>("hibernate-detector") {
      public HibernateFacetConfiguration detectFacet(final VirtualFile source,
                                               final Collection<HibernateFacetConfiguration> existentFacetConfigurations) {
        if (!existentFacetConfigurations.isEmpty()) {
          return existentFacetConfigurations.iterator().next();
        }
        final HibernateFacetConfiguration configuration = createDefaultConfiguration();
        configuration.getDescriptorsConfiguration().addConfigFile(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA, source.getUrl());
        return configuration;
      }
    };
    detectorRegistry.registerUniversalDetector(StdFileTypes.XML, PlatformPatterns.virtualFile().xmlWithRootTag(string().oneOf(HibernateConstants.CFG_XML_ROOT_TAG)), facetDetector);
  }
}
