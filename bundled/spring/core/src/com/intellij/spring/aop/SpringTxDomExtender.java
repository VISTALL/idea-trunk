/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.aop;

import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.tx.Advice;
import com.intellij.spring.model.xml.tx.AnnotationDriven;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class SpringTxDomExtender extends DomExtender<Beans> {

  public void registerExtensions(@NotNull final Beans element, @NotNull final DomExtensionsRegistrar registrar) {
    registrar.registerCollectionChildrenExtension(new XmlName("advice", SpringConstants.TX_NAMESPACE_KEY), Advice.class);
    registrar.registerCollectionChildrenExtension(new XmlName("annotation-driven", SpringConstants.TX_NAMESPACE_KEY), AnnotationDriven.class);
  }

}