/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.util.FixedChildColumnInfo;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossActivationConfig;
import com.fuhrer.idea.jboss.model.JBossActivationProperty;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.reflect.DomGenericInfo;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

class JBossActivationConfigEditor extends DomCollectionControl<JBossActivationProperty> {

    private final JBossActivationConfig config;

    JBossActivationConfigEditor(JBossActivationConfig config) {
        super(config, "activation-config-property");
        this.config = config;
        getComponent().setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossActivationConfigEditor.title")));
    }

    @Override
    protected String getEmptyPaneText() {
        return JBossBundle.getText("JBossActivationConfigEditor.empty");
    }

    @Override
    protected ColumnInfo<?, ?>[] createColumnInfos(DomElement parent) {
        DomGenericInfo info = parent.getManager().getGenericInfo(JBossActivationProperty.class);
        return new ColumnInfo<?, ?>[]{
                new FixedChildColumnInfo<JBossActivationProperty>(JBossBundle.getText("JBossActivationConfigEditor.name"), info, "activation-config-property-name", JavaeeBundle.getIcon("/nodes/j2eeParameter.png")),
                new FixedChildColumnInfo<JBossActivationProperty>(JBossBundle.getText("JBossActivationConfigEditor.value"), info, "activation-config-property-value", JavaeeBundle.getIcon("/nodes/j2eeParameter.png"))
        };
    }

    @Override
    protected AnAction[] createAdditionActions() {
        AnAction action = new ControlAddAction() {
            @Override
            protected void afterAddition(JTable table, int index) {
                table.editCellAt(index, 0);
            }
        };
        action.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), getComponent());
        return new AnAction[]{action};
    }

    @Override
    protected void doRemove(List<JBossActivationProperty> list) {
        super.doRemove(list);
        if (config.getActivationConfigProperties().isEmpty()) {
            new WriteCommandAction<Object>(getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    config.undefine();
                }
            }.execute();
        }
    }
}
