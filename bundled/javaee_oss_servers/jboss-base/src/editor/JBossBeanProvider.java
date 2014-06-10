/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBeanProvider;
import com.fuhrer.idea.jboss.JBossUtil;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JBossBeanProvider extends JavaeeBeanProvider {

    JBossBeanProvider() {
        super(15);
    }

    @Override
    protected boolean acceptEntityBean(@NotNull EntityBean bean) {
        EjbFacet facet = EjbModuleUtil.getEjbFacet(bean);
        return (JBossUtil.getEjbRoot(facet) != null) && (JBossUtil.getCmpRoot(facet) != null);
    }

    @Override
    protected boolean acceptSessionBean(@NotNull SessionBean bean) {
        return JBossUtil.getEjbRoot(EjbModuleUtil.getEjbFacet(bean)) != null;
    }

    @Override
    protected boolean acceptMessageBean(@NotNull MessageDrivenBean bean) {
        return JBossUtil.getEjbRoot(EjbModuleUtil.getEjbFacet(bean)) != null;
    }

    @Override
    @Nullable
    protected PerspectiveFileEditor createEntityBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull EntityBean bean) {
        return new JBossEntityEditor(bean).getFileEditor();
    }

    @Override
    @Nullable
    protected PerspectiveFileEditor createSessionBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull SessionBean bean) {
        return new JBossSessionEditor(bean).getFileEditor();
    }

    @Override
    @Nullable
    protected PerspectiveFileEditor createMessageBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull MessageDrivenBean bean) {
        return new JBossMessageEditor(bean).getFileEditor();
    }
}
