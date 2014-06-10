package com.intellij.coldFusion.UI.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public class CfmlFacet extends Facet<CfmlFacetConfiguration> {
    public static final FacetTypeId<CfmlFacet> ID = new FacetTypeId<CfmlFacet>("cfml");

    public CfmlFacet(@NotNull FacetType facetType, @NotNull Module module, String name, @NotNull CfmlFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }
}