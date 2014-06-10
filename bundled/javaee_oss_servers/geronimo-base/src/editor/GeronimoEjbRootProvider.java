/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class GeronimoEjbRootProvider extends JavaeeRootProvider<GeronimoEjbRoot, EjbFacet> {

    GeronimoEjbRootProvider() {
        super(GeronimoEjbRoot.class, EjbFacet.ID, 22);
    }

    @Override
    protected GeronimoEjbRoot getEditedElement(@NotNull EjbFacet facet, VirtualFile file) {
        return GeronimoUtil.getEjbRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull GeronimoEjbRoot root, @NotNull EjbFacet facet) {
        return new GeronimoEjbRootEditor(root);
    }
}
