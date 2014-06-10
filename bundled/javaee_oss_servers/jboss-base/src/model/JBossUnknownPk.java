/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossUnknownPk extends JavaeeDomModelElement {

    GenericDomValue<String> getKeyGeneratorFactory();

    GenericDomValue<PsiClass> getUnknownPkClass();

    GenericDomValue<String> getFieldName();

    GenericDomValue<Boolean> getReadOnly();

    GenericDomValue<Integer> getReadTimeOut();

    GenericDomValue<String> getColumnName();

    GenericDomValue<String> getJdbcType();

    GenericDomValue<String> getSqlType();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getAutoIncrement();
}
