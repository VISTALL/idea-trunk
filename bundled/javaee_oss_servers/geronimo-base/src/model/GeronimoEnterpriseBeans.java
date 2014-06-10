/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.SubTagList;
import com.intellij.util.xml.SubTagsList;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoEnterpriseBeans extends JavaeeDomModelElement {

    @SubTagList(value = "entity")
    List<GeronimoEntityBean> getEntityBeans();

    @SubTagList(value = "session")
    List<GeronimoSessionBean> getSessionBeans();

    @SubTagList(value = "message-driven")
    List<GeronimoMessageBean> getMessageBeans();

    @SubTagsList(value = {"session", "entity", "message-driven"})
    List<JavaeeDomModelElement> getEnterpriseBeans();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "entity")
    GeronimoEntityBean addEntityBean();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "session")
    GeronimoSessionBean addSessionBean();

    @SubTagsList(value = {"entity", "session", "message-driven"}, tagName = "message-driven")
    GeronimoMessageBean addMessageBean();
}
