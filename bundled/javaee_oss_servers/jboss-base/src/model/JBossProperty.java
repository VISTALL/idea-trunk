/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossProperty extends JavaeeDomModelElement {

    GenericDomValue<String> getPropertyName();

    GenericDomValue<String> getColumnName();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getNotNull();

    GenericDomValue<String> getJdbcType();

    GenericDomValue<String> getSqlType();
}
