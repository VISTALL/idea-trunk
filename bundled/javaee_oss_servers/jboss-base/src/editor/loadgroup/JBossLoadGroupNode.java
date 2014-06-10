/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class JBossLoadGroupNode extends JavaeeObjectDescriptor<JBossLoadGroup> {

    private final EntityBean bean;

    JBossLoadGroupNode(JavaeeNodeDescriptor<?> parent, EntityBean bean, JBossLoadGroup group) {
        super(group, parent, null);
        this.bean = bean;
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getLoadGroupName().getValue();
    }

    @Override
    protected Icon getNewOpenIcon() {
        return JavaeeBundle.getIcon("/javaee/persistenceEntity.png");
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        for (CmpField field : bean.getCmpFields()) {
            list.add(new JBossCmpFieldNode(this, field));
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }

    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }
}
