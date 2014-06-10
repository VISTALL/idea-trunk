/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeEnableEditor;
import com.fuhrer.idea.jboss.model.JBossAudit;
import com.fuhrer.idea.jboss.model.JBossAuditField;
import com.intellij.openapi.util.Factory;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.ComboControl;
import com.intellij.util.xml.ui.DomStringWrapper;
import com.intellij.util.xml.ui.DomUIFactory;
import com.intellij.util.xml.ui.TextPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

abstract class JBossAuditFieldEditor extends JavaeeEnableEditor<JBossAuditField, JBossAudit> {

    private JPanel panel;

    private TextPanel fieldName;

    private TextPanel columnName;

    private JComboBox jdbcType;

    private TextPanel sqlType;

    JBossAuditFieldEditor(String title, JBossAudit audit) {
        super(title, audit);
        DomManager manager = audit.getManager();
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getFieldName();
            }
        }))).bind(fieldName);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getColumnName();
            }
        }))).bind(columnName);
        addComponent(new ComboControl(new DomStringWrapper(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getJdbcType();
            }
        })), JBossDataFactory.getFactory(JBossDataFactory.getTypes()))).bind(jdbcType);
        addComponent(DomUIFactory.createControl(manager.createStableValue(new Factory<GenericDomValue<String>>() {
            public GenericDomValue<String> create() {
                return getElement().getSqlType();
            }
        }))).bind(sqlType);
        setContent(panel);
        getComponent().setBorder(BorderFactory.createTitledBorder(title));
    }

    @Override
    protected void undefined(@NotNull JBossAudit parent) {
        if (isDeleted(parent.getCreatedBy()) && isDeleted(parent.getCreatedTime()) && isDeleted(parent.getUpdatedBy()) && isDeleted(parent.getUpdatedTime())) {
            parent.undefine();
        }
    }

    private boolean isDeleted(JBossAuditField field) {
        return field.getXmlTag() == null;
    }
}
