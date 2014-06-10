/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossDbDefaults extends JBossUnknownPkHolder {

    GenericDomValue<String> getDatasource();

    GenericDomValue<String> getDatasourceMapping();

    GenericDomValue<Boolean> getCreateTable();

    GenericDomValue<Boolean> getAlterTable();

    GenericDomValue<Boolean> getRemoveTable();

    GenericDomValue<Boolean> getPkConstraint();

    GenericDomValue<Boolean> getFkConstraint();

    GenericDomValue<Boolean> getRowLocking();

    GenericDomValue<Boolean> getCleanReadAheadOnLoad();

    GenericDomValue<Boolean> getReadOnly();

    GenericDomValue<Integer> getReadTimeOut();

    GenericDomValue<Integer> getListCacheMax();

    GenericDomValue<Integer> getFetchSize();

    GenericDomValue<String> getPreferredRelationMapping();

    JBossDbReadAhead getReadAhead();
}
