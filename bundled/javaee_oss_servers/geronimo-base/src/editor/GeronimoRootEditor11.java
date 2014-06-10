/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoModuleId;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;

import javax.swing.*;

class GeronimoRootEditor11 extends JavaeeBaseEditor {

    private final GeronimoCommonRoot root;

    private JPanel panel;

    private TextPanel group;

    private TextPanel artifact;

    private TextPanel version;

    private TextPanel type;

    GeronimoRootEditor11(final GeronimoCommonRoot root) {
        this.root = root;
        DomManager manager = root.getManager();
        final GeronimoModuleId id = manager.createStableValue(new Factory<GeronimoModuleId>() {
            public GeronimoModuleId create() {
                return root.getEnvironment().getModuleId();
            }
        });
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return id.getGroupId();
            }
        }))).bind(group);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return id.getArtifactId();
            }
        }))).bind(artifact);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return id.getVersion();
            }
        }))).bind(version);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return id.getType();
            }
        }))).bind(type);
    }

    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void commit() {
        super.commit();
        final GeronimoModuleId id = root.getEnvironment().getModuleId();
        if (id.getXmlTag() != null) {
            if (isEmpty(id.getGroupId()) && isEmpty(id.getArtifactId()) && isEmpty(id.getVersion()) && isEmpty(id.getType())) {
                new WriteCommandAction<Object>(root.getManager().getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        id.undefine();
                    }
                }.execute();
            }
        }
    }

    private boolean isEmpty(GenericDomValue<String> value) {
        return StringUtil.isEmpty(value.getValue());
    }
}
