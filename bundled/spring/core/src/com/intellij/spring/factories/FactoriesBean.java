/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

public class FactoriesBean {
  @Tag("factories")
  @AbstractCollection(surroundWithTag = false)
  public FactoryBeanInfo[] myFactories;


  public FactoryBeanInfo[] getFactories() {
    return myFactories;
  }
}