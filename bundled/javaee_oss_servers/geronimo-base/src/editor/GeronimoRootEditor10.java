/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class GeronimoRootEditor10 extends JavaeeBaseEditor {

    private JPanel panel;

    private TextPanel configId;

    private TextPanel parentId;

    GeronimoRootEditor10(final GeronimoCommonRoot root) {
        DomManager manager = root.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return root.getConfigId();
            }
        }))).bind(configId);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return root.getParentId();
            }
        }))).bind(parentId);
    }

    public JComponent getComponent() {
        return panel;
    }
}
