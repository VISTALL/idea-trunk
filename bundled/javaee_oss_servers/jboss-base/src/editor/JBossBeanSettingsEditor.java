/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.javaee.util.DomBooleanWrapper;
import com.fuhrer.idea.javaee.util.TripleCheckBox;
import com.fuhrer.idea.javaee.util.TripleCheckBoxControl;
import com.fuhrer.idea.jboss.model.JBossNamedBean;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class JBossBeanSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel remote;

    private TextPanel local;

    private TripleCheckBox callByValue;

    private TripleCheckBox clustered;

    JBossBeanSettingsEditor(final JBossNamedBean ejb) {
        DomManager manager = ejb.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getJndiName();
            }
        }))).bind(remote);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getLocalJndiName();
            }
        }))).bind(local);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return ejb.getCallByValue();
            }
        })))).bind(callByValue);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return ejb.getClustered();
            }
        })))).bind(clustered);
    }

    public JComponent getComponent() {
        return panel;
    }
}
