/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/lang

package com.intellij.spring.model.xml.lang;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.beans.TypedBeanPointerAttribute;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/lang:dynamicScriptType interface.
 */
public interface CustomizableScript extends DomElement, SimpleScript {

  /**
   * Returns the value of the customizer-ref child.
   * <pre>
   * <h3>Attribute null:customizer-ref documentation</h3>
   * 	Reference to a GroovyObjectCustomizer or similar customizer bean.
   *
   * </pre>
   * @return the value of the customizer-ref child.
   */
  @RequiredBeanType("org.springframework.scripting.groovy.GroovyObjectCustomizer")
  @NotNull
  TypedBeanPointerAttribute getCustomizerRef();

}
