/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class GeronimoWebRootProvider extends JavaeeRootProvider<GeronimoWebRoot, WebFacet> {

    GeronimoWebRootProvider() {
        super(GeronimoWebRoot.class, WebFacet.ID, 24);
    }

    @Override
    protected GeronimoWebRoot getEditedElement(@NotNull WebFacet facet, VirtualFile file) {
        return GeronimoUtil.getWebRoot(facet);
    }

    @Override
    protected CommittablePanel createPanel(@NotNull GeronimoWebRoot root, @NotNull WebFacet facet) {
        return new GeronimoWebRootEditor(root);
    }
}
