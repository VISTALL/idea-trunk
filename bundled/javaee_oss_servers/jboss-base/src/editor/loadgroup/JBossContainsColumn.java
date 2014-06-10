/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.Nullable;

class JBossContainsColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, Boolean> {

    JBossContainsColumn() {
        super(JBossBundle.getText("JBossLoadGroupsEditor.contains"));
    }

    @Override
    public Class<?> getColumnClass() {
        return Boolean.class;
    }

    @Override
    @Nullable
    public Boolean valueOf(JavaeeNodeDescriptor<?> item) {
        if (item.getElement() instanceof CmpField) {
            return findField((JBossLoadGroup) item.getParentDescriptor().getElement(), ((CmpField) item.getElement()).getFieldName().getValue()) != null;
        }
        return null;
    }

    @Override
    public void setValue(final JavaeeNodeDescriptor<?> item, Boolean value) {
        if (item.getElement() instanceof CmpField) {
            final GenericDomValue<CmpField> field = findField((JBossLoadGroup) item.getParentDescriptor().getElement(), ((CmpField) item.getElement()).getFieldName().getValue());
            if ((field == null) && value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        GenericDomValue<CmpField> value = ((JBossLoadGroup) item.getParentDescriptor().getElement()).addFieldName();
                        value.setValue((CmpField) item.getElement());
                    }
                }.execute();
            } else if ((field != null) && !value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        field.undefine();
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return item.getElement() instanceof CmpField;
    }

    @Nullable
    private GenericDomValue<CmpField> findField(JBossLoadGroup group, String name) {
        if (name != null) {
            for (GenericDomValue<CmpField> value : group.getFieldNames()) {
                CmpField field = value.getValue();
                if ((field != null) && name.equals(field.getFieldName().getValue())) {
                    return value;
                }
            }
        }
        return null;
    }
}
