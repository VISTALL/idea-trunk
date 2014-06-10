/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.aop;

import com.intellij.aop.AopAdvice;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomElement;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public interface SpringAopAdvice extends AopAdvice, DomElement {

  @NotNull
  SpringAdvisedElementsSearcher getSearcher();

  GenericAttributeValue<Integer> getOrder();
}
