/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.model.JBossMessageBean;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class JBossMessageSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel destinationName;

    private TextPanel localName;

    private TextPanel user;

    private TextPanel password;

    private TextPanel clientId;

    private TextPanel subscriptionId;

    JBossMessageSettingsEditor(final JBossMessageBean ejb) {
        DomManager manager = ejb.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getDestinationJndiName();
            }
        }))).bind(destinationName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getLocalJndiName();
            }
        }))).bind(localName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getMdbUser();
            }
        }))).bind(user);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getMdbPasswd();
            }
        }))).bind(password);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getMdbClientId();
            }
        }))).bind(clientId);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getMdbSubscriptionId();
            }
        }))).bind(subscriptionId);
    }

    public JComponent getComponent() {
        return panel;
    }
}
