/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peter
*/
public class CustomBeanInfo {
  public String beanName;
  public String beanClassName;
  public String factoryBeanName;
  public String factoryMethodName;
  @NonNls public String idAttribute;
  public List<Integer> path = new ArrayList<Integer>();
  public int constructorArgumentCount;

  public CustomBeanInfo() {
  }

  public CustomBeanInfo(CustomBeanInfo info) {
    beanName = info.beanName;
    beanClassName = info.beanClassName;
    factoryBeanName = info.factoryBeanName;
    factoryMethodName = info.factoryMethodName;
    idAttribute = info.idAttribute;
    path = info.path;
    constructorArgumentCount = info.constructorArgumentCount;
  }
}
