/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model;

import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.util.xml.SubTagList;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface JBossCmpBeans extends JavaeeDomModelElement {

    @SubTagList(value = "entity")
    List<JBossCmpBean> getCmpBeans();

    @SubTagList(value = "entity")
    JBossCmpBean addCmpBean();
}
