/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class JBossAppRootProvider extends JavaeeRootProvider<JBossAppRoot, JavaeeApplicationFacet> {

    JBossAppRootProvider() {
        super(JBossAppRoot.class, JavaeeApplicationFacet.ID, 11);
    }

    @Override
    protected JBossAppRoot getEditedElement(@NotNull JavaeeApplicationFacet facet, VirtualFile file) {
        return JBossUtil.getAppRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull JBossAppRoot root, @NotNull JavaeeApplicationFacet facet) {
        return new JBossAppRootEditor(facet.getRoot(), root);
    }
}
