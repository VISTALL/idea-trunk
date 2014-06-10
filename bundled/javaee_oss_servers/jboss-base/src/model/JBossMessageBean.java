/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.fuhrer.idea.javaee.converter.MessageBeanConverter;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossMessageBean extends JBossEjbBean {

    @Convert(value = MessageBeanConverter.class)
    GenericDomValue<MessageDrivenBean> getEjbName();

    GenericDomValue<String> getDestinationJndiName();

    GenericDomValue<String> getLocalJndiName();

    GenericDomValue<String> getMdbUser();

    GenericDomValue<String> getMdbPasswd();

    GenericDomValue<String> getMdbClientId();

    GenericDomValue<String> getMdbSubscriptionId();

    JBossActivationConfig getActivationConfig();
}
