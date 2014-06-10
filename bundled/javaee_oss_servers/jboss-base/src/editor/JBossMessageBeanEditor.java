/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.reference.JBossReferenceEditor;
import com.fuhrer.idea.jboss.model.JBossActivationConfig;
import com.fuhrer.idea.jboss.model.JBossMessageBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Factory;

import javax.swing.*;

class JBossMessageBeanEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossMessageBeanEditor(MessageDrivenBean bean, final JBossMessageBean ejb) {
        JBossActivationConfig config = bean.getManager().createStableValue(new Factory<JBossActivationConfig>() {
            public JBossActivationConfig create() {
                return ejb.getActivationConfig();
            }
        });
        Splitter left = createSplitter(new JBossMessageSettingsEditor(ejb), new JBossActivationConfigEditor(config), true);
        splitter = createSplitter(left, new JBossReferenceEditor(bean, ejb), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
