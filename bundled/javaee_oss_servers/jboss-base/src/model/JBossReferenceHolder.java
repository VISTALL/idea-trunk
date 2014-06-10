/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossReferenceHolder extends JavaeeDomModelElement {

    List<JBossEjbRef> getEjbRefs();

    JBossEjbRef addEjbRef();

    List<JBossEjbLocalRef> getEjbLocalRefs();

    JBossEjbLocalRef addEjbLocalRef();

    List<JBossResourceRef> getResourceRefs();

    JBossResourceRef addResourceRef();

    List<JBossResourceEnvRef> getResourceEnvRefs();

    JBossResourceEnvRef addResourceEnvRef();

    List<JBossMessageDestinationRef> getMessageDestinationRefs();

    JBossMessageDestinationRef addMessageDestinationRef();
}
