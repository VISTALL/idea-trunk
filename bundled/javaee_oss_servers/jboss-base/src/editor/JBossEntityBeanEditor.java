/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.editor.cmpfield.JBossCmpFieldsEditor;
import com.fuhrer.idea.jboss.editor.loadgroup.JBossLoadGroupsEditor;
import com.fuhrer.idea.jboss.editor.reference.JBossReferenceEditor;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossEntityBean;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class JBossEntityBeanEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossEntityBeanEditor(EntityBean bean, JBossEntityBean ejb, JBossCmpBean cmp) {
        if (JBossEjbUtil.isCmpBean(bean)) {
            splitter = createSplitter(createUpper(bean, ejb), createLower(bean, cmp), true);
        } else {
            splitter = createUpper(bean, ejb);
        }
    }

    private Splitter createUpper(EntityBean bean, JBossEntityBean ejb) {
        return createSplitter(new JBossBeanSettingsEditor(ejb), new JBossReferenceEditor(bean, ejb), false);
    }

    private Splitter createLower(EntityBean bean, JBossCmpBean cmp) {
        return createSplitter(createLeftPane(bean, cmp), createRightPane(cmp), false);
    }

    private JTabbedPane createLeftPane(EntityBean bean, JBossCmpBean cmp) {
        JTabbedPane pane = new JTabbedPane();
        addContent(pane, new JBossCmpFieldsEditor(bean, cmp), JBossBundle.getText("JBossCmpFieldsEditor.title"));
        addContent(pane, new JBossLoadGroupsEditor(bean, cmp), JBossBundle.getText("JBossLoadGroupsEditor.title"));
        addContent(pane, new JBossBeanDefaultsEditor(cmp), JBossBundle.getText("JBossBeanDefaultsEditor.title"));
        return pane;
    }

    private JTabbedPane createRightPane(JBossCmpBean cmp) {
        JTabbedPane pane = new JTabbedPane();
        addContent(pane, new JBossOptimisticLockingEditor(cmp), JBossBundle.getText("JBossOptimisticLockingEditor.title"));
        addContent(pane, new JBossBeanReadAheadEditor(cmp), JBossBundle.getText("JBossBeanReadAheadEditor.title"));
        addContent(pane, new JBossUnknownPkEditor(cmp), JBossBundle.getText("JBossUnknownPkEditor.title"));
        addContent(pane, new JBossAuditEditor(cmp), JBossBundle.getText("JBossAuditEditor.title"));
        return pane;
    }

    public JComponent getComponent() {
        return splitter;
    }
}
