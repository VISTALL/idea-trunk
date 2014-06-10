/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JBossPropertyDialog extends DialogWrapper {

    private final JBossProperty property;

    private JPanel panel;

    private JTextField columnName;

    private JComboBox jdbcType;

    private JTextField sqlType;

    private JCheckBox notNull;

    public JBossPropertyDialog(JBossProperty property) {
        super(property.getManager().getProject(), false);
        this.property = property;
        setTitle(JBossBundle.getText("JBossPropertyDialog.title", property.getPropertyName().getValue()));
        columnName.setText(property.getColumnName().getValue());
        jdbcType.setModel(new DefaultComboBoxModel(JBossDataFactory.getTypes()));
        jdbcType.setSelectedItem(property.getJdbcType().getValue());
        sqlType.setText(property.getSqlType().getValue());
        notNull.setSelected(Boolean.TRUE.equals(property.getNotNull().getValue()));
        init();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return columnName;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        new WriteCommandAction<Object>(property.getManager().getProject()) {
            @Override
            protected void run(Result<Object> result) throws Throwable {
                property.getColumnName().setValue(columnName.getText());
                property.getJdbcType().setValue((String) jdbcType.getSelectedItem());
                property.getSqlType().setValue(sqlType.getText());
                property.getNotNull().setValue(notNull.isSelected());
            }
        }.execute();
    }
}
