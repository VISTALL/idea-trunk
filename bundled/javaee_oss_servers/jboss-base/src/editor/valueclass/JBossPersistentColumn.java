/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossProperty;
import com.fuhrer.idea.jboss.model.JBossValueClass;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.ui.ColumnInfo;

class JBossPersistentColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, Boolean> {

    JBossPersistentColumn() {
        super(JBossBundle.getText("JBossValueClassesEditor.persistent"));
    }

    @Override
    public Class<?> getColumnClass() {
        return Boolean.class;
    }

    @Override
    public Boolean valueOf(JavaeeNodeDescriptor<?> item) {
        Boolean value = null;
        Object element = item.getElement();
        if (element instanceof JBossPropertyDescriptor) {
            value = (JBossValueClassesEditor.findProperty(item) != null) ? Boolean.TRUE : Boolean.FALSE;
        }
        return value;
    }

    @Override
    public void setValue(final JavaeeNodeDescriptor<?> item, Boolean value) {
        Object element = item.getElement();
        if (element instanceof JBossPropertyDescriptor) {
            final JBossProperty property = JBossValueClassesEditor.findProperty(item);
            if ((property == null) && value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        JBossValueClass valueClass = (JBossValueClass) item.getParentDescriptor().getElement();
                        String name = ((JBossPropertyDescriptor) item.getElement()).getName();
                        valueClass.addProperty().getPropertyName().setValue(name);
                    }
                }.execute();
            } else if ((property != null) && !value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        property.undefine();
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return item.getElement() instanceof JBossPropertyDescriptor;
    }
}
