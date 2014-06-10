/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class JBossAppSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel jmxName;

    private TextPanel securityDomain;

    private TextPanel unauthenticatedPrincipal;

    JBossAppSettingsEditor(final JBossAppRoot app) {
        DomManager manager = app.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return app.getJmxName();
            }
        }))).bind(jmxName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return app.getSecurityDomain();
            }
        }))).bind(securityDomain);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return app.getUnauthenticatedPrincipal();
            }
        }))).bind(unauthenticatedPrincipal);
    }

    public JComponent getComponent() {
        return panel;
    }
}
