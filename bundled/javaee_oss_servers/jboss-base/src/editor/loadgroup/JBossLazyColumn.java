/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.Nullable;

class JBossLazyColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, Boolean> {

    private final JBossCmpBean cmp;

    JBossLazyColumn(JBossCmpBean cmp) {
        super(JBossBundle.getText("JBossLoadGroupsEditor.lazy"));
        this.cmp = cmp;
    }

    @Override
    public Class<?> getColumnClass() {
        return Boolean.class;
    }

    @Override
    @Nullable
    public Boolean valueOf(JavaeeNodeDescriptor<?> item) {
        if (item.getElement() instanceof JBossLoadGroup) {
            return findGroup(((JBossLoadGroup) item.getElement()).getLoadGroupName().getValue()) != null;
        }
        return null;
    }

    @Override
    public void setValue(final JavaeeNodeDescriptor<?> item, Boolean value) {
        if (item.getElement() instanceof JBossLoadGroup) {
            final GenericDomValue<JBossLoadGroup> group = findGroup(((JBossLoadGroup) item.getElement()).getLoadGroupName().getValue());
            if ((group == null) && value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        GenericDomValue<JBossLoadGroup> value = cmp.getLazyLoadGroups().addLoadGroupName();
                        value.setValue((JBossLoadGroup) item.getElement());
                    }
                }.execute();
            } else if ((group != null) && !value) {
                new WriteCommandAction<Object>(item.getProject()) {
                    @Override
                    protected void run(Result<Object> result) throws Throwable {
                        group.undefine();
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return item.getElement() instanceof JBossLoadGroup;
    }

    @Nullable
    private GenericDomValue<JBossLoadGroup> findGroup(String name) {
        if (name != null) {
            for (GenericDomValue<JBossLoadGroup> value : cmp.getLazyLoadGroups().getLoadGroupNames()) {
                JBossLoadGroup group = value.getValue();
                if ((group != null) && name.equals(group.getLoadGroupName().getValue())) {
                    return value;
                }
            }
        }
        return null;
    }
}
