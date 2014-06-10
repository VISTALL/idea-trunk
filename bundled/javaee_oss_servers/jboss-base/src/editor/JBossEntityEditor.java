/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeMockEditor;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossEntityBean;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.ui.DomFileEditor;
import com.intellij.util.xml.ui.EditedElementDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JBossEntityEditor extends JavaeeMockEditor {

    JBossEntityEditor(@NotNull EntityBean bean) {
        this(bean, EjbModuleUtil.getEjbFacet(bean));
    }

    private JBossEntityEditor(@NotNull final EntityBean bean, final EjbFacet facet) {
        super(facet);
        JBossEntityBean ejb = addEditedElement(JBossEntityBean.class, new EditedElementDescription<JBossEntityBean>() {
            @Override
            public JBossEntityBean find() {
                return JBossEjbUtil.findEntityBean(JBossUtil.getEjbRoot(facet), bean);
            }

            @Override
            public void initialize(JBossEntityBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public JBossEntityBean addElement() {
                JBossEjbRoot root = JBossUtil.getEjbRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addEntityBean() : null;
            }
        });
        JBossCmpBean cmp = addEditedElement(JBossCmpBean.class, new EditedElementDescription<JBossCmpBean>() {
            @Override
            public JBossCmpBean find() {
                return JBossEjbUtil.findCmpBean(JBossUtil.getCmpRoot(facet), bean);
            }

            @Override
            public void initialize(JBossCmpBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public JBossCmpBean addElement() {
                JBossCmpRoot root = JBossUtil.getCmpRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addCmpBean() : null;
            }
        });
        DomFileEditor<?> editor = initEditor(new JBossEntityBeanEditor(bean, ejb, cmp), bean);
        addWatchedElement(editor, JBossUtil.getEjbRoot(facet));
        addWatchedElement(editor, JBossUtil.getCmpRoot(facet));
    }
}
