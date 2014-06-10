/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class GlassfishWebRootProvider extends JavaeeRootProvider<GlassfishWebRoot, WebFacet> {

    GlassfishWebRootProvider() {
        super(GlassfishWebRoot.class, WebFacet.ID, 34);
    }

    @Override
    protected GlassfishWebRoot getEditedElement(@NotNull WebFacet facet, VirtualFile file) {
        return GlassfishUtil.getWebRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull GlassfishWebRoot root, @NotNull WebFacet facet) {
        return new GlassfishWebRootEditor(facet.getRoot(), root);
    }
}
