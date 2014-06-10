/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossEjbRefConverter;
import com.intellij.javaee.model.xml.EjbRef;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossEjbRef extends JavaeeDomModelElement {

    @Convert(value = JBossEjbRefConverter.class)
    GenericDomValue<EjbRef> getEjbRefName();

    GenericDomValue<String> getJndiName();
}
