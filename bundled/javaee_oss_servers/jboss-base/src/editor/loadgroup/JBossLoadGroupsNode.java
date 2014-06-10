/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class JBossLoadGroupsNode extends JavaeeNodeDescriptor<JBossCmpBean> {

    private final EntityBean bean;

    JBossLoadGroupsNode(EntityBean bean, JBossCmpBean cmp) {
        super(cmp.getManager().getProject(), null, null, cmp);
        this.bean = bean;
    }

    @Override
    @Nullable
    protected String getNewNodeText() {
        return null;
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        for (JBossLoadGroup group : getElement().getLoadGroups().getLoadGroups()) {
            list.add(new JBossLoadGroupNode(this, bean, (JBossLoadGroup) group.createStableCopy()));
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
