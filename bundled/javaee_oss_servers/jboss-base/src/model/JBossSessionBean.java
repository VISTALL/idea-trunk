/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.javaee.converter.SessionBeanConverter;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossSessionBean extends JBossEjbBean, JBossNamedBean {

    @Convert(value = SessionBeanConverter.class)
    GenericDomValue<SessionBean> getEjbName();
}
