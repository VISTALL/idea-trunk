/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.jboss.model.converter.JBossCmpFieldConverter;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossCmpField extends JBossPropertyHolder {

    @Convert(value = JBossCmpFieldConverter.class)
    GenericDomValue<CmpField> getFieldName();

    GenericDomValue<Boolean> getReadOnly();

    GenericDomValue<String> getReadTimeOut();

    GenericDomValue<String> getColumnName();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getNotNull();

    GenericDomValue<String> getJdbcType();

    GenericDomValue<String> getSqlType();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getAutoIncrement();

    @SubTag(indicator = true)
    GenericDomValue<Boolean> getDbindex();

    GenericDomValue<Boolean> getCheckDirtyAfterGet();

    GenericDomValue<String> getStateFactory();
}
