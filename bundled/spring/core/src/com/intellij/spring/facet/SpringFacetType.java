/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.VirtualFilePattern;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringFacetType extends FacetType<SpringFacet, SpringFacetConfiguration> {

  public final static SpringFacetType INSTANCE = new SpringFacetType();

  private SpringFacetType() {
    super(SpringFacet.FACET_TYPE_ID, "Spring", SpringBundle.message("spring"));
  }

  public SpringFacetConfiguration createDefaultConfiguration() {
    return new SpringFacetConfiguration();
  }

  public SpringFacet createFacet(@NotNull final Module module,
                                 final String name,
                                 @NotNull final SpringFacetConfiguration configuration,
                                 final Facet underlyingFacet) {
    return new SpringFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public Icon getIcon() {
    return SpringIcons.SPRING_ICON;
  }

  @Override
  public String getHelpTopic() {
    return "IntelliJ.IDEA.Procedures.Java.EE.Development.Managing.Facets.Facet.Specific.Settings.Spring";
  }

  public void registerDetectors(final FacetDetectorRegistry<SpringFacetConfiguration> registry) {
    @NonNls final String rootTag = "beans";
    VirtualFilePattern pattern = PlatformPatterns.virtualFile().xmlWithRootTag(StandardPatterns.string().equalTo(rootTag));
    registry.registerUniversalDetector(StdFileTypes.XML, pattern, new FacetDetector<VirtualFile, SpringFacetConfiguration>("spring-detector") {
      public SpringFacetConfiguration detectFacet(final VirtualFile source,
                                                  final Collection<SpringFacetConfiguration> existentFacetConfigurations) {
        if (!existentFacetConfigurations.isEmpty()) {
          return existentFacetConfigurations.iterator().next();
        }
        return new SpringFacetConfiguration();
      }
    });
  }
}
