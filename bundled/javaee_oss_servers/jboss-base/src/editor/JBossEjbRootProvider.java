/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class JBossEjbRootProvider extends JavaeeRootProvider<JBossEjbRoot, EjbFacet> {

    JBossEjbRootProvider() {
        super(JBossEjbRoot.class, EjbFacet.ID, 12);
    }

    @Override
    protected String getTitle() {
        return JBossBundle.getText("JBossEjbRootEditor.title");
    }

    @Override
    protected JBossEjbRoot getEditedElement(@NotNull EjbFacet facet, VirtualFile file) {
        return JBossUtil.getEjbRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull JBossEjbRoot root, @NotNull EjbFacet facet) {
        return new JBossEjbRootEditor(facet.getXmlRoot(), root);
    }
}
