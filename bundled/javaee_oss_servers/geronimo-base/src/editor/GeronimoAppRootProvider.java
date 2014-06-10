/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;
import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class GeronimoAppRootProvider extends JavaeeRootProvider<GeronimoAppRoot, JavaeeApplicationFacet> {

    GeronimoAppRootProvider() {
        super(GeronimoAppRoot.class, JavaeeApplicationFacet.ID, 21);
    }

    @Override
    protected GeronimoAppRoot getEditedElement(@NotNull JavaeeApplicationFacet facet, VirtualFile file) {
        return GeronimoUtil.getAppRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull GeronimoAppRoot root, @NotNull JavaeeApplicationFacet facet) {
        return new GeronimoAppRootEditor(root);
    }
}
