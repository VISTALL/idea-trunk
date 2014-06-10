/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor;

import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class GlassfishWebSettingsEditor extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel contextRoot;

    GlassfishWebSettingsEditor(final GlassfishWebRoot web) {
        DomManager manager = web.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return web.getContextRoot();
            }
        }))).bind(contextRoot);
    }

    public JComponent getComponent() {
        return panel;
    }
}
