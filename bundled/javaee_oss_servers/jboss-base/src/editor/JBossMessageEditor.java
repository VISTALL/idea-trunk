/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeMockEditor;
import com.fuhrer.idea.jboss.JBossUtil;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossMessageBean;
import com.intellij.javaee.ejb.EjbModuleUtil;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.util.xml.ui.DomFileEditor;
import com.intellij.util.xml.ui.EditedElementDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JBossMessageEditor extends JavaeeMockEditor {

    JBossMessageEditor(@NotNull MessageDrivenBean bean) {
        this(bean, EjbModuleUtil.getEjbFacet(bean));
    }

    private JBossMessageEditor(@NotNull final MessageDrivenBean bean, final EjbFacet facet) {
        super(facet);
        JBossMessageBean ejb = addEditedElement(JBossMessageBean.class, new EditedElementDescription<JBossMessageBean>() {
            @Override
            public JBossMessageBean find() {
                return JBossEjbUtil.findMessageBean(JBossUtil.getEjbRoot(facet), bean);
            }

            @Override
            public void initialize(JBossMessageBean element) {
                element.getEjbName().setValue(bean);
            }

            @Override
            @Nullable
            public JBossMessageBean addElement() {
                JBossEjbRoot root = JBossUtil.getEjbRoot(facet);
                return (root != null) ? root.getEnterpriseBeans().addMessageBean() : null;
            }
        });
        DomFileEditor<?> editor = initEditor(new JBossMessageBeanEditor(bean, ejb), bean);
        addWatchedElement(editor, JBossUtil.getEjbRoot(facet));
        addWatchedElement(editor, JBossUtil.getCmpRoot(facet));
    }
}
