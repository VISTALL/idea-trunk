/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.aop;

import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.SpringModelElement;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class SpringAopDomExtender extends DomExtender<SpringModelElement> {

  public void registerExtensions(@NotNull final SpringModelElement element, @NotNull final DomExtensionsRegistrar registrar) {
    registrar.registerCollectionChildrenExtension(new XmlName("config", SpringConstants.AOP_NAMESPACE_KEY), AopConfig.class);
  }

}