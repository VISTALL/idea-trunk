/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossAppRoot extends JBossSecurityRoleHolder {

    GenericDomValue<String> getJmxName();

    GenericDomValue<String> getSecurityDomain();

    GenericDomValue<String> getUnauthenticatedPrincipal();
}
