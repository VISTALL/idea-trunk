/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishSecurityRoleHolder;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class GlassfishSecurityRolesNode extends JavaeeNodeDescriptor<GlassfishSecurityRoleWrapper> {

    private final GlassfishSecurityRoleHolder holder;

    GlassfishSecurityRolesNode(GlassfishSecurityRoleWrapper wrapper, GlassfishSecurityRoleHolder holder) {
        super(holder.getManager().getProject(), null, null, wrapper);
        this.holder = holder;
    }

    @Override
    @Nullable
    protected String getNewNodeText() {
        return null;
    }

    @Override
    public JavaeeNodeDescriptor<?>[] getChildren() {
        List<JavaeeNodeDescriptor<?>> list = new ArrayList<JavaeeNodeDescriptor<?>>();
        for (SecurityRole role : getElement().getSecurityRoles()) {
            list.add(new GlassfishSecurityRoleNode(holder, this, role));
        }
        return list.toArray(new JavaeeNodeDescriptor[list.size()]);
    }
}
