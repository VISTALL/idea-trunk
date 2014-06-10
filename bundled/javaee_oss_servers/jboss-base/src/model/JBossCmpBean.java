/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.javaee.converter.EntityBeanConverter;
import com.fuhrer.idea.jboss.model.converter.JBossLoadGroupConverter;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossCmpBean extends JBossUnknownPkHolder {

    @Convert(value = EntityBeanConverter.class)
    GenericDomValue<EntityBean> getEjbName();

    GenericDomValue<String> getDatasource();

    GenericDomValue<String> getDatasourceMapping();

    GenericDomValue<String> getTableName();

    GenericDomValue<Boolean> getCreateTable();

    GenericDomValue<Boolean> getRemoveTable();

    GenericDomValue<Boolean> getPkConstraint();

    GenericDomValue<Boolean> getRowLocking();

    GenericDomValue<Boolean> getCleanReadAheadOnLoad();

    GenericDomValue<Boolean> getReadOnly();

    GenericDomValue<Integer> getReadTimeOut();

    GenericDomValue<Integer> getListCacheMax();

    GenericDomValue<Integer> getFetchSize();

    List<JBossCmpField> getCmpFields();

    JBossCmpField addCmpField();

    JBossLoadGroups getLoadGroups();

    @Convert(value = JBossLoadGroupConverter.class)
    GenericDomValue<JBossLoadGroup> getEagerLoadGroup();

    JBossLazyGroups getLazyLoadGroups();

    JBossOptimisticLocking getOptimisticLocking();

    JBossBeanReadAhead getReadAhead();

    JBossAudit getAudit();
}
