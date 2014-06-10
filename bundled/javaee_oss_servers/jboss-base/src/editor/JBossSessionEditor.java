/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeMockEditor;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossSessionBean;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.util.xml.ui.DomFileEditor;
import com.intellij.util.xml.ui.EditedElementDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JBossSessionEditor extends JavaeeMockEditor {

    JBossSessionEditor(@NotNull SessionBean bean) {
        this(bean, EjbModuleUtil.getEjbFacet(bean));
    }

    private JBossSessionEditor(@NotNull final SessionBean bean, final EjbFacet facet) {
        super(facet);
        JBossSessionBean ejb = addEditedElement(JBossSessionBean.class, new EditedElementDescription<JBossSessionBean>() {
            @Override
            public JBossSessionBean find() {
                return JBossEjbUtil.findSessionBean(JBossUtil.getEjbRoot(facet), bean);
            }

            @Override
            public void initialize(JBossSessionBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public JBossSessionBean addElement() {
                JBossEjbRoot root = JBossUtil.getEjbRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addSessionBean() : null;
            }
        });
        DomFileEditor<?> editor = initEditor(new JBossSessionBeanEditor(bean, ejb), bean);
        addWatchedElement(editor, JBossUtil.getEjbRoot(facet));
    }
}
