/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossUnknownPkHolder extends JavaeeDomModelElement {

    JBossUnknownPk getUnknownPk();
}
