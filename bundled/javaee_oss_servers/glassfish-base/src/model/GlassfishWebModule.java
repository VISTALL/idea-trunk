/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.model;

import com.fuhrer.idea.javaee.converter.WebModuleConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GlassfishWebModule extends JavaeeDomModelElement {

    @Convert(value = WebModuleConverter.class)
    GenericDomValue<JavaeeModule> getWebUri();

    GenericDomValue<String> getContextRoot();
}
