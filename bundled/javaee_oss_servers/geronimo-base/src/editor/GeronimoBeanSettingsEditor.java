/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoNamedBean;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class GeronimoBeanSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel remote;

    private TextPanel local;

    GeronimoBeanSettingsEditor(final GeronimoNamedBean bean) {
        DomManager manager = bean.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return bean.getJndiName();
            }
        }))).bind(remote);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return bean.getLocalJndiName();
            }
        }))).bind(local);
    }

    public JComponent getComponent() {
        return panel;
    }
}
