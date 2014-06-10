/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossNamedBean extends JavaeeDomModelElement {

    GenericDomValue<String> getJndiName();

    GenericDomValue<String> getLocalJndiName();

    GenericDomValue<Boolean> getCallByValue();

    GenericDomValue<Boolean> getClustered();
}
