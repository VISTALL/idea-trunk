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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationFacet extends Facet<BeanValidationFacetConfiguration> {
  public final static FacetTypeId<BeanValidationFacet> FACET_TYPE_ID = new FacetTypeId<BeanValidationFacet>("BeanValidation");

  public BeanValidationFacet(final FacetType facetType, final Module module, final String name, final BeanValidationFacetConfiguration configuration, final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  @Nullable
  public static BeanValidationFacet getInstance(Module module) {
    return FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID);
  }
}
