/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossActivationProperty extends JavaeeDomModelElement {

    GenericDomValue<String> getActivationConfigPropertyName();

    GenericDomValue<String> getActivationConfigPropertyValue();
}
