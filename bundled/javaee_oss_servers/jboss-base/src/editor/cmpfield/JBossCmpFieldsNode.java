/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.cmpfield;

import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class JBossCmpFieldsNode extends JavaeeNodeDescriptor<JBossCmpBean> {

    private final EntityBean bean;

    JBossCmpFieldsNode(EntityBean bean, JBossCmpBean cmp) {
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
        for (CmpField field : bean.getCmpFields()) {
            list.add(new JBossCmpFieldNode(this, field));
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
