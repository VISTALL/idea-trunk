/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.model;

import com.fuhrer.idea.javaee.converter.SecurityRoleConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GlassfishSecurityRole extends JavaeeDomModelElement {

    @Convert(value = SecurityRoleConverter.class)
    GenericDomValue<SecurityRole> getRoleName();

    List<GlassfishPrincipal> getPrincipalNames();

    GlassfishPrincipal addPrincipalName();
}
