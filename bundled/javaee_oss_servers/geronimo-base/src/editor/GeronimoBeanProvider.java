/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.javaee.editor.JavaeeBeanProvider;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GeronimoBeanProvider extends JavaeeBeanProvider {

    GeronimoBeanProvider() {
        super(25);
    }

    @Override
    protected boolean acceptEntityBean(@NotNull EntityBean bean) {
        return GeronimoUtil.getEjbRoot(EjbModuleUtil.getEjbFacet(bean)) != null;
    }

    @Override
    protected boolean acceptSessionBean(@NotNull SessionBean bean) {
        return GeronimoUtil.getEjbRoot(EjbModuleUtil.getEjbFacet(bean)) != null;
    }

    @Override
    @Nullable
    protected PerspectiveFileEditor createEntityBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull EntityBean bean) {
        return new GeronimoEntityEditor(bean).getFileEditor();
    }

    @Override
    @Nullable
    protected PerspectiveFileEditor createSessionBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull SessionBean bean) {
        return new GeronimoSessionEditor(bean).getFileEditor();
    }
}
