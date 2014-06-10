/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishPrincipal;
import com.fuhrer.idea.glassfish.model.GlassfishSecurityRole;
import com.fuhrer.idea.glassfish.model.GlassfishSecurityRoleHolder;
import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class GlassfishSecurityRoleNode extends JavaeeObjectDescriptor<SecurityRole> {

    private final GlassfishSecurityRoleHolder holder;

    GlassfishSecurityRoleNode(GlassfishSecurityRoleHolder holder, GlassfishSecurityRolesNode parent, SecurityRole role) {
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
        GlassfishSecurityRole role = GlassfishSecurityRoleUtil.findSecurityRole(holder, getElement());
        if (role != null) {
            for (GlassfishPrincipal principal : role.getPrincipalNames()) {
                list.add(new GlassfishPrincipalNode(this, principal));
            }
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
