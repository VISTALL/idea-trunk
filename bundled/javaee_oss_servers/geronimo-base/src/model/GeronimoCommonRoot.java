/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoCommonRoot extends JavaeeDomModelElement {

    @Attribute("configId")
    GenericAttributeValue<String> getConfigId();

    @Attribute("parentId")
    GenericAttributeValue<String> getParentId();

    @SubTag("environment")
    GeronimoEnvironment getEnvironment();
}
