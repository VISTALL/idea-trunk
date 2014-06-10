/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoModuleId extends JavaeeDomModelElement {

    @SubTag("groupId")
    GenericDomValue<String> getGroupId();

    @SubTag("artifactId")
    GenericDomValue<String> getArtifactId();

    @SubTag("version")
    GenericDomValue<String> getVersion();

    @SubTag("type")
    GenericDomValue<String> getType();
}
