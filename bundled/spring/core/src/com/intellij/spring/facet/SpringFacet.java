/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringFacet extends Facet<SpringFacetConfiguration> {
  public final static FacetTypeId<SpringFacet> FACET_TYPE_ID = new FacetTypeId<SpringFacet>("spring");

  public SpringFacet(final FacetType facetType, final Module module, final String name, final SpringFacetConfiguration configuration, final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    Disposer.register(this, configuration);
  }

  @Nullable
  public static SpringFacet getInstance(Module module) {
    return FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID);
  }
}
