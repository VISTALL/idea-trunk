/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeRootProvider;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.CommittablePanel;
import org.jetbrains.annotations.NotNull;

class JBossCmpRootProvider extends JavaeeRootProvider<JBossCmpRoot, EjbFacet> {

    JBossCmpRootProvider() {
        super(JBossCmpRoot.class, EjbFacet.ID, 13);
    }

    @Override
    protected String getTitle() {
        return JBossBundle.getText("JBossCmpRootEditor.title");
    }

    @Override
    protected JBossCmpRoot getEditedElement(@NotNull EjbFacet facet, VirtualFile file) {
        return JBossUtil.getCmpRoot(facet);
    }

    @Override
    @NotNull
    protected CommittablePanel createPanel(@NotNull JBossCmpRoot root, @NotNull EjbFacet facet) {
        return new JBossCmpRootEditor(root);
    }
}
