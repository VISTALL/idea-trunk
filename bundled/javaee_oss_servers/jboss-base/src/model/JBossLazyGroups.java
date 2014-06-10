/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossLoadGroupConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossLazyGroups extends JavaeeDomModelElement {

    @Convert(value = JBossLoadGroupConverter.class)
    List<GenericDomValue<JBossLoadGroup>> getLoadGroupNames();

    GenericDomValue<JBossLoadGroup> addLoadGroupName();
}
