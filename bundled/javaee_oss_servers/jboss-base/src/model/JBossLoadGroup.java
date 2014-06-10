/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossCmpFieldConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossLoadGroup extends JavaeeDomModelElement {

    GenericDomValue<String> getLoadGroupName();

    GenericDomValue<String> getDescription();

    @Convert(value = JBossCmpFieldConverter.class)
    List<GenericDomValue<CmpField>> getFieldNames();

    GenericDomValue<CmpField> addFieldName();
}
