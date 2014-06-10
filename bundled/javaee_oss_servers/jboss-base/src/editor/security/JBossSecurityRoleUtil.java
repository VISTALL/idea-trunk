/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.jboss.model.JBossSecurityRole;
import com.fuhrer.idea.jboss.model.JBossSecurityRoleHolder;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

class JBossSecurityRoleUtil {

    private JBossSecurityRoleUtil() {
    }

    @Nullable
    static JBossSecurityRole findSecurityRole(JBossSecurityRoleHolder holder, final SecurityRole source) {
        return (source == null) ? null : ContainerUtil.find(holder.getSecurityRoles(), new Condition<JBossSecurityRole>() {
            public boolean value(JBossSecurityRole target) {
                return source.equals(target.getRoleName().getValue());
            }
        });
    }
}
