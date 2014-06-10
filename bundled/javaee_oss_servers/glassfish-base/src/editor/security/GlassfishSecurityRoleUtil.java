/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishSecurityRole;
import com.fuhrer.idea.glassfish.model.GlassfishSecurityRoleHolder;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

class GlassfishSecurityRoleUtil {

    private GlassfishSecurityRoleUtil() {
    }

    @Nullable
    static GlassfishSecurityRole findSecurityRole(GlassfishSecurityRoleHolder holder, final SecurityRole source) {
        return (source == null) ? null : ContainerUtil.find(holder.getSecurityRoleMappings(), new Condition<GlassfishSecurityRole>() {
            public boolean value(GlassfishSecurityRole target) {
                return source.equals(target.getRoleName().getValue());
            }
        });
    }
}
