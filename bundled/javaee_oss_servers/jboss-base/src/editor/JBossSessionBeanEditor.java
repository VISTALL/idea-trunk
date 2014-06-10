/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.reference.JBossReferenceEditor;
import com.fuhrer.idea.jboss.model.JBossSessionBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;

class JBossSessionBeanEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossSessionBeanEditor(SessionBean bean, JBossSessionBean ejb) {
        splitter = createSplitter(new JBossBeanSettingsEditor(ejb), new JBossReferenceEditor(bean, ejb), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
