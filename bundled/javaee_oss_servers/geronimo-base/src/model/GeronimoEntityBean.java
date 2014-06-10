/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.model;

import com.fuhrer.idea.javaee.converter.EntityBeanConverter;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GeronimoEntityBean extends GeronimoNamedBean {

    @Convert(value = EntityBeanConverter.class)
    GenericDomValue<EntityBean> getEjbName();
}
