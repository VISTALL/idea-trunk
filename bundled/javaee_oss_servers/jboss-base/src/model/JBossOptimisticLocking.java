/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossLoadGroupConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossOptimisticLocking extends JavaeeDomModelElement {

    @Convert(value = JBossLoadGroupConverter.class)
    GenericDomValue<JBossLoadGroup> getGroupName();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getModifiedStrategy();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getReadStrategy();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getVersionColumn();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getTimestampColumn();

    GenericDomValue<String> getKeyGeneratorFactory();

    GenericDomValue<PsiClass> getFieldType();

    GenericDomValue<String> getFieldName();

    GenericDomValue<String> getColumnName();

    GenericDomValue<String> getJdbcType();

    GenericDomValue<String> getSqlType();
}
