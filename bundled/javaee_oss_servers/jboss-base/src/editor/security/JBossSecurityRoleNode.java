/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.model.JBossPrincipal;
import com.fuhrer.idea.jboss.model.JBossSecurityRole;
import com.fuhrer.idea.jboss.model.JBossSecurityRoleHolder;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class JBossSecurityRoleNode extends JavaeeObjectDescriptor<SecurityRole> {

    private final JBossSecurityRoleHolder holder;

    JBossSecurityRoleNode(JBossSecurityRoleHolder holder, JBossSecurityRolesNode parent, SecurityRole role) {
        super(role, parent, null);
        this.holder = holder;
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getRoleName().getValue();
    }

    @Override
    protected Icon getNewOpenIcon() {
        return JavaeeBundle.getIcon("/nodes/SecurityRole.png");
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        JBossSecurityRole role = JBossSecurityRoleUtil.findSecurityRole(holder, getElement());
        if (role != null) {
            for (JBossPrincipal principal : role.getPrincipalNames()) {
                list.add(new JBossPrincipalNode(this, principal));
            }
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
