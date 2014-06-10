/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.xml.util.SpringConstant;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public interface SpringInjection extends SpringValueHolder, SpringValueHolderDefinition {

  /**
   * Returns the value of the meta child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:meta documentation</h3>
   * 	Arbitrary metadata attached to a bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the meta child.
   */
  @NotNull
  Meta getMeta();

  /**
   * Returns the value of the description child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:description documentation</h3>
   * 	Contains informative text describing the purpose of the enclosing
   * 	element.
   * 	Used primarily for user documentation of XML bean definition documents.
   * <p/>
   * </pre>
   *
   * @return the value of the description child.
   */
  @NotNull
  GenericDomValue<String> getDescription();

  @SubTag("constant")
  SpringConstant getConstant();
}
