/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossResourceEnvRefConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.ResourceEnvRef;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossResourceEnvRef extends JavaeeDomModelElement {

    @Convert(value = JBossResourceEnvRefConverter.class)
    GenericDomValue<ResourceEnvRef> getResourceEnvRefName();

    GenericDomValue<String> getJndiName();
}
