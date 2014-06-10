/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.fuhrer.idea.javaee.converter.MessageBeanConverter;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoMessageBean extends JavaeeDomModelElement {

    @Convert(value = MessageBeanConverter.class)
    GenericDomValue<MessageDrivenBean> getEjbName();
}
