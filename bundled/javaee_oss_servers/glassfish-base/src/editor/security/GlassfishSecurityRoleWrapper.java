/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import com.intellij.javaee.model.xml.web.WebApp;

import java.util.Collections;
import java.util.List;

abstract class GlassfishSecurityRoleWrapper {

    abstract List<SecurityRole> getSecurityRoles();

    static GlassfishSecurityRoleWrapper get(final JavaeeApplication app) {
        return new GlassfishSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (app != null) ? app.getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }

    static GlassfishSecurityRoleWrapper get(final EjbJar ejb) {
        return new GlassfishSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (ejb != null) ? ejb.getAssemblyDescriptor().getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }

    static GlassfishSecurityRoleWrapper get(final WebApp web) {
        return new GlassfishSecurityRoleWrapper() {
            @Override
            List<SecurityRole> getSecurityRoles() {
                return (web != null) ? web.getSecurityRoles() : Collections.<SecurityRole>emptyList();
            }
        };
    }
}
