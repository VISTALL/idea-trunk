/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("factoryBean")
public class FactoryBeanInfo {
  @Attribute("factory")
  public String myFactory;

  @Attribute("objectType")
  public String myObjectType;

  @Attribute("propertyNames")
  public String myPropertyNames;

  public String getFactory() {
    return myFactory;
  }

  public String getObjectType() {
    return myObjectType;
  }

  public String getPropertyNames() {
    return myPropertyNames;
  }
}