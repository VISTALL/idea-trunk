/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import com.intellij.javaee.model.xml.web.WebApp;

import java.util.Collections;
import java.util.List;

abstract class JBossSecurityRoleWrapper {

    abstract List<SecurityRole> getSecurityRoles();

    static JBossSecurityRoleWrapper get(final JavaeeApplication app) {
        return new JBossSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (app != null) ? app.getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }

    static JBossSecurityRoleWrapper get(final EjbJar ejb) {
        return new JBossSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (ejb != null) ? ejb.getAssemblyDescriptor().getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }

    static JBossSecurityRoleWrapper get(final WebApp web) {
        return new JBossSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (web != null) ? web.getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }
}
