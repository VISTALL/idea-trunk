/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GlassfishSecurityRoleHolder extends JavaeeDomModelElement {

    List<GlassfishSecurityRole> getSecurityRoleMappings();

    GlassfishSecurityRole addSecurityRoleMapping();
}
