/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.javaee.converter.SecurityRoleConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossSecurityRole extends JavaeeDomModelElement {

    @Convert(value = SecurityRoleConverter.class)
    GenericDomValue<SecurityRole> getRoleName();

    List<JBossPrincipal> getPrincipalNames();

    JBossPrincipal addPrincipalName();
}
