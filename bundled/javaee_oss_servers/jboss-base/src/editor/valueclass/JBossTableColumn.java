/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.util.IconTableCellRenderer;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.table.*;

class JBossTableColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, String> {

    private final TableCellRenderer renderer = new IconTableCellRenderer(JavaeeBundle.getIcon("/nodes/DataTables.png"));

    JBossTableColumn() {
        super(JBossBundle.getText("JBossValueClassesEditor.table.column"));
    }

    @Override
    public TableCellRenderer getRenderer(JavaeeNodeDescriptor<?> item) {
        return renderer;
    }

    @Override
    public String valueOf(JavaeeNodeDescriptor<?> item) {
        String value = null;
        Object element = item.getElement();
        if (element instanceof JBossPropertyDescriptor) {
            JBossProperty property = JBossValueClassesEditor.findProperty(item);
            if (property != null) {
                value = property.getColumnName().getValue();
            }
        }
        return value;
    }

    @Override
    public void setValue(JavaeeNodeDescriptor<?> item, final String value) {
        Object element = item.getElement();
        if (element instanceof JBossPropertyDescriptor) {
            final JBossProperty property = JBossValueClassesEditor.findProperty(item);
            if (property != null) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        property.getColumnName().setValue("".equals(value) ? null : value);
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return (item.getElement() instanceof JBossPropertyDescriptor) && (JBossValueClassesEditor.findProperty(item) != null);
    }
}
