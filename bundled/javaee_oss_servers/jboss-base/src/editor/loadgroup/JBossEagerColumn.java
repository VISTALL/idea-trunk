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
import org.jetbrains.annotations.Nullable;

class JBossEagerColumn extends ColumnInfo<JavaeeNodeDescriptor<?>, Boolean> {

    private final JBossCmpBean cmp;

    JBossEagerColumn(JBossCmpBean cmp) {
        super(JBossBundle.getText("JBossLoadGroupsEditor.eager"));
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
            JBossLoadGroup group = cmp.getEagerLoadGroup().getValue();
            String name = (group == null) ? null : group.getLoadGroupName().getValue();
            return (name != null) && name.equals(((JBossLoadGroup) item.getElement()).getLoadGroupName().getValue());
        }
        return null;
    }

    @Override
    public void setValue(final JavaeeNodeDescriptor<?> item, final Boolean value) {
        if (item.getElement() instanceof JBossLoadGroup) {
            new WriteCommandAction<Object>(item.getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    if (value) {
                        cmp.getEagerLoadGroup().setValue((JBossLoadGroup) item.getElement());
                    } else {
                        cmp.getEagerLoadGroup().undefine();
                    }
                }
            }.execute();
        }
    }

    @Override
    public boolean isCellEditable(JavaeeNodeDescriptor<?> item) {
        return item.getElement() instanceof JBossLoadGroup;
    }
}
