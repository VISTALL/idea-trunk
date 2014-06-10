package com.intellij.coldFusion.UI.facet;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 02.04.2009
 * Time: 18:37:53
 * To change this template use File | Settings | File Templates.
 */
public class CfmlFacetType extends FacetType<CfmlFacet, CfmlFacetConfiguration> {
    private CfmlFacetType() {
        super(CfmlFacet.ID, "ColdFusion", "ColdFusion");
    }

    public CfmlFacetConfiguration createDefaultConfiguration() {
        return new CfmlFacetConfiguration();
    }

    public CfmlFacet createFacet(@NotNull Module module, String name, @NotNull CfmlFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new CfmlFacet(this, module, name, configuration, underlyingFacet);
    }

    public boolean isSuitableModuleType(ModuleType moduleType) {
        // TODO: insert checking
        return true;
    }

    @Override
    public boolean isOnlyOneFacetAllowed() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return CfmlIcons.FILETYPE_ICON;
    }
}
