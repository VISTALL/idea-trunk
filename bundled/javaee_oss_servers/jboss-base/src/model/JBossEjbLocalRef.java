/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossEjbLocalRefConverter;
import com.intellij.javaee.model.xml.EjbLocalRef;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossEjbLocalRef extends JavaeeDomModelElement {

    @Convert(value = JBossEjbLocalRefConverter.class)
    GenericDomValue<EjbLocalRef> getEjbRefName();

    GenericDomValue<String> getLocalJndiName();
}
