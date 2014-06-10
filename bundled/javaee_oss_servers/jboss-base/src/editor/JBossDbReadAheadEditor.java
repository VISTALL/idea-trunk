/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.javaee.util.ButtonGroupControl;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossDbDefaults;
import com.fuhrer.idea.jboss.model.JBossDbReadAhead;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomStringWrapper;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossDbReadAheadEditor extends JavaeeEnableEditor<JBossDbReadAhead, JBossDbDefaults> {

    private JPanel panel;

    private JRadioButton none;

    private JRadioButton onLoad;

    private JRadioButton onFind;

    private TextPanel loadGroup;

    private TextPanel pageSize;

    JBossDbReadAheadEditor(@NotNull JBossDbDefaults defaults) {
        super(JBossBundle.getText("JBossDbReadAheadEditor.title"), defaults);
        DomManager manager = defaults.getManager();
        addComponent(new ButtonGroupControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getStrategy();
            }
        })), none, onLoad, onFind)).bind(null);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getEagerLoadGroup();
            }
        }))).bind(loadGroup);
        addComponent(DomUIFactory.createTextControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<Integer>>() {
            public GenericDomValue<Integer> create() {
                return getElement().getPageSize();
            }
        })))).bind(pageSize);
        setContent(panel);
    }

    @Override
    protected JBossDbReadAhead createElement(@NotNull JBossDbDefaults parent) {
        return parent.getReadAhead();
    }
}
