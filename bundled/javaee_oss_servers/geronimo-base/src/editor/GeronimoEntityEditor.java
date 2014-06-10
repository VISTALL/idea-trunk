/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEntityBean;
import com.fuhrer.idea.javaee.editor.JavaeeMockEditor;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.ui.DomFileEditor;
import com.intellij.util.xml.ui.EditedElementDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GeronimoEntityEditor extends JavaeeMockEditor {

    GeronimoEntityEditor(@NotNull EntityBean bean) {
        this(bean, EjbModuleUtil.getEjbFacet(bean));
    }

    private GeronimoEntityEditor(@NotNull final EntityBean bean, final EjbFacet facet) {
        super(facet);
        GeronimoEntityBean ejb = addEditedElement(GeronimoEntityBean.class, new EditedElementDescription<GeronimoEntityBean>() {
            @Override
            public GeronimoEntityBean find() {
                return GeronimoEjbUtil.findEntityBean(GeronimoUtil.getEjbRoot(facet), bean);
            }

            @Override
            public void initialize(GeronimoEntityBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public GeronimoEntityBean addElement() {
                GeronimoEjbRoot root = GeronimoUtil.getEjbRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addEntityBean() : null;
            }
        });
        DomFileEditor<?> editor = initEditor(new GeronimoBeanSettingsEditor(ejb), bean);
        addWatchedElement(editor, GeronimoUtil.getEjbRoot(facet));
    }
}
