/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.javaee.util.DomBooleanWrapper;
import com.fuhrer.idea.javaee.util.TripleCheckBox;
import com.fuhrer.idea.javaee.util.TripleCheckBoxControl;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class JBossWebSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel contextRoot;

    private TextPanel securityDomain;

    private TripleCheckBox useSessionCookies;

    JBossWebSettingsEditor(final JBossWebRoot web) {
        DomManager manager = web.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return web.getContextRoot();
            }
        }))).bind(contextRoot);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return web.getSecurityDomain();
            }
        }))).bind(securityDomain);
        addComponent(new TripleCheckBoxControl(new DomBooleanWrapper(manager.createStableValue(new Factory<GenericDomValue<Boolean>>() {
            public GenericDomValue<Boolean> create() {
                return web.getUseSessionCookies();
            }
        })))).bind(useSessionCookies);
    }

    public JComponent getComponent() {
        return panel;
    }
}
