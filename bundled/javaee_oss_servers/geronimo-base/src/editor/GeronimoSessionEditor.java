/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoSessionBean;
import com.fuhrer.idea.javaee.editor.JavaeeMockEditor;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.util.xml.ui.DomFileEditor;
import com.intellij.util.xml.ui.EditedElementDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GeronimoSessionEditor extends JavaeeMockEditor {

    GeronimoSessionEditor(@NotNull SessionBean bean) {
        this(bean, EjbModuleUtil.getEjbFacet(bean));
    }

    private GeronimoSessionEditor(@NotNull final SessionBean bean, final EjbFacet facet) {
        super(facet);
        GeronimoSessionBean ejb = addEditedElement(GeronimoSessionBean.class, new EditedElementDescription<GeronimoSessionBean>() {
            @Override
            public GeronimoSessionBean find() {
                return GeronimoEjbUtil.findSessionBean(GeronimoUtil.getEjbRoot(facet), bean);
            }

            @Override
            public void initialize(GeronimoSessionBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public GeronimoSessionBean addElement() {
                GeronimoEjbRoot root = GeronimoUtil.getEjbRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addSessionBean() : null;
            }
        });
        DomFileEditor<?> editor = initEditor(new GeronimoBeanSettingsEditor(ejb), bean);
        addWatchedElement(editor, GeronimoUtil.getEjbRoot(facet));
    }
}
