/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class JBossWebRootProvider extends JavaeeRootProvider<JBossWebRoot, WebFacet> {

    JBossWebRootProvider() {
        super(JBossWebRoot.class, WebFacet.ID, 14);
    }

    @Override
    protected JBossWebRoot getEditedElement(@NotNull WebFacet facet, VirtualFile file) {
        return JBossUtil.getWebRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull JBossWebRoot root, @NotNull WebFacet facet) {
        return new JBossWebRootEditor(facet.getRoot(), root);
    }
}
