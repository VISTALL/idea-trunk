/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.SubTagList;
import com.intellij.util.xml.SubTagsList;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossEnterpriseBeans extends JavaeeDomModelElement {

    @SubTagList(value = "entity")
    List<JBossEntityBean> getEntityBeans();

    @SubTagList(value = "session")
    List<JBossSessionBean> getSessionBeans();

    @SubTagList(value = "message-driven")
    List<JBossMessageBean> getMessageBeans();

    @SubTagsList(value = {"session", "entity", "message-driven"})
    List<JBossEjbBean> getEnterpriseBeans();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "entity")
    JBossEntityBean addEntityBean();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "session")
    JBossSessionBean addSessionBean();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "message-driven")
    JBossMessageBean addMessageBean();
}
