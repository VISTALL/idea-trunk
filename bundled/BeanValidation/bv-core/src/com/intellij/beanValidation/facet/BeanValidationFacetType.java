/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.facet;

import com.intellij.beanValidation.BVIcons;
import static com.intellij.beanValidation.constants.BvCommonConstants.*;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.impl.autodetecting.FacetDetectorRegistryEx;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationFacetType extends FacetType<BeanValidationFacet, BeanValidationFacetConfiguration> {

  public final static BeanValidationFacetType INSTANCE = new BeanValidationFacetType();

  private BeanValidationFacetType() {
    super(BeanValidationFacet.FACET_TYPE_ID, "BeanValidation", BVBundle.message("bv.framework.name"));
  }

  public BeanValidationFacetConfiguration createDefaultConfiguration() {
    return new BeanValidationFacetConfiguration();
  }

  public BeanValidationFacet createFacet(@NotNull final Module module, final String name, @NotNull final BeanValidationFacetConfiguration configuration,
                                 final Facet underlyingFacet) {
    return new BeanValidationFacet(this, module, name, configuration, underlyingFacet);
  }

  public boolean isSuitableModuleType(final ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  public Icon getIcon() {
    return BVIcons.BEAN_VALIDATION_ICON;
  }

   public void registerDetectors(final FacetDetectorRegistry<BeanValidationFacetConfiguration> facetDetectorRegistry) {
    FacetDetectorRegistryEx<BeanValidationFacetConfiguration> registry = (FacetDetectorRegistryEx<BeanValidationFacetConfiguration>)facetDetectorRegistry;
    registry.registerUniversalDetectorByFileNameAndRootTag(BEAN_VALIDATION_CONFIG_FILENAME, BEAN_VALIDATION_CONFIG_ROOT_TAG_NAME, new BeanValidationFacetDetector(), null);         
  }

  private static class BeanValidationFacetDetector extends FacetDetector<VirtualFile, BeanValidationFacetConfiguration> {
    private BeanValidationFacetDetector() {
      super("beanValidation-detector");
    }

    public BeanValidationFacetConfiguration detectFacet(final VirtualFile source, final Collection<BeanValidationFacetConfiguration> existentFacetConfigurations) {
      Iterator<BeanValidationFacetConfiguration> iterator = existentFacetConfigurations.iterator();
      if (iterator.hasNext()) {
        return iterator.next();
      }

      return new BeanValidationFacetConfiguration();
    }
  }
}
