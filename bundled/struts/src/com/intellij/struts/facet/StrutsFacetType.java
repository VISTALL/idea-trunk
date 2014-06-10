/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.impl.autodetecting.FacetDetectorRegistryEx;
import com.intellij.facet.ui.DefaultFacetSettingsEditor;
import com.intellij.facet.ui.FacetEditor;
import com.intellij.facet.ui.MultipleFacetSettingsEditor;
import com.intellij.j2ee.web.WebUtilImpl;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.facet.ui.MultipleStrutsFacetEditor;
import com.intellij.struts.facet.ui.StrutsFacetDefaultSettingsEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author nik
 */
public class StrutsFacetType extends FacetType<StrutsFacet, StrutsFacetConfiguration> {
  public static final FacetTypeId<StrutsFacet> ID = new FacetTypeId<StrutsFacet>("struts");
  public static final StrutsFacetType INSTANCE = new StrutsFacetType();

  public StrutsFacetType() {
    super(ID, "struts", "Struts", WebFacet.ID);
  }

  public StrutsFacetConfiguration createDefaultConfiguration() {
    return new StrutsFacetConfiguration();
  }

  public StrutsFacet createFacet(@NotNull final Module module, final String name, @NotNull final StrutsFacetConfiguration configuration, @Nullable final Facet underlyingFacet) {
    return new StrutsFacet(this, module, name, configuration, underlyingFacet);
  }

  public void registerDetectors(final FacetDetectorRegistry<StrutsFacetConfiguration> facetDetectorRegistry) {
    FacetDetectorRegistryEx<StrutsFacetConfiguration> registry = (FacetDetectorRegistryEx<StrutsFacetConfiguration>)facetDetectorRegistry;
    registry.registerUniversalDetectorByFileNameAndRootTag(AddStrutsSupportUtil.STRUTS_CONFIG_FILE_NAME, "struts-config",
                                                           new StrutsFacetDetector(), WebUtilImpl.BY_PARENT_WEB_ROOT_SELECTOR);
  }

  public Icon getIcon() {
    return StrutsIcons.ACTION_ICON;
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts.facet";
  }

  public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public DefaultFacetSettingsEditor createDefaultConfigurationEditor(@NotNull final Project project, @NotNull final StrutsFacetConfiguration configuration) {
    return new StrutsFacetDefaultSettingsEditor(configuration.getValidationConfiguration());
  }

  public MultipleFacetSettingsEditor createMultipleConfigurationsEditor(@NotNull final Project project, @NotNull final FacetEditor[] editors) {
    return new MultipleStrutsFacetEditor(editors);
  }

  private static class StrutsFacetDetector extends FacetDetector<VirtualFile, StrutsFacetConfiguration> {
    private StrutsFacetDetector() {
      super("struts-detector");
    }

    public StrutsFacetConfiguration detectFacet(final VirtualFile source, final Collection<StrutsFacetConfiguration> existentFacetConfigurations) {
      Iterator<StrutsFacetConfiguration> iterator = existentFacetConfigurations.iterator();
      if (iterator.hasNext()) {
        return iterator.next();
      }
      return new StrutsFacetConfiguration();
    }
  }
}
