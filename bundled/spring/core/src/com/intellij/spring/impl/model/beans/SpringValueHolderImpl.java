/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.beans;

import com.intellij.spring.model.xml.beans.SpringValueHolder;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringRef;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;

/**
 * @author peter
 */
public abstract class SpringValueHolderImpl extends SpringElementsHolderImpl implements SpringValueHolder {

  public GenericDomValue<SpringBeanPointer> getRefElement() {
    final SpringRef springRef = getRef();
    if (DomUtil.hasXml(springRef.getBean())) return springRef.getBean();
    if (DomUtil.hasXml(springRef.getLocal())) return springRef.getLocal();
    if (DomUtil.hasXml(springRef.getParentAttr())) return springRef.getParentAttr();
    return getRefAttr();
  }

  public GenericDomValue<?> getValueElement() {
    final SpringValue springValue = getValue();
    if (!DomUtil.hasXml(springValue)) return getValueAttr();
    return springValue;
  }
}
