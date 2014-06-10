/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossWebRoot extends JBossReferenceHolder, JBossSecurityRoleHolder {

    GenericDomValue<String> getContextRoot();

    GenericDomValue<String> getSecurityDomain();

    List<GenericDomValue<String>> getVirtualHosts();

    GenericDomValue<Boolean> getUseSessionCookies();
}
