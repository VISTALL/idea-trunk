/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.javaee.util.ButtonGroupControl;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomStringWrapper;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossEjbSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel jmxName;

    private TextPanel securityDomain;

    private TextPanel unauthenticatedPrincipal;

    private JRadioButton none;

    private JRadioButton excluded;

    private JRadioButton unchecked;

    JBossEjbSettingsEditor(@NotNull final JBossEjbRoot ejb) {
        DomManager manager = ejb.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getJmxName();
            }
        }))).bind(jmxName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getSecurityDomain();
            }
        }))).bind(securityDomain);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getUnauthenticatedPrincipal();
            }
        }))).bind(unauthenticatedPrincipal);
        addComponent(new ButtonGroupControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return ejb.getMissingMethodPermissionsExcludedMode();
            }
        })), none, excluded, unchecked)).bind(null);
    }

    public JComponent getComponent() {
        return panel;
    }
}
