/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

public class SpringBeanScope {
  public static SpringBeanScope SINGLETON_SCOPE = new SpringBeanScope("singleton");
  public static SpringBeanScope PROROTYPE_SCOPE = new SpringBeanScope("prototype");
  public static SpringBeanScope SESSION_SCOPE = new SpringBeanScope("session");
  public static SpringBeanScope GLOBAL_SESSION_SCOPE = new SpringBeanScope("globalSession");
  public static SpringBeanScope REQUEST_SCOPE = new SpringBeanScope("request");
  public static SpringBeanScope BUNDLE_SCOPE = new SpringBeanScope("bundle");

  private final String value;

  public SpringBeanScope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SpringBeanScope[] getDefaultScopes() {
    return new SpringBeanScope[] {SINGLETON_SCOPE, PROROTYPE_SCOPE, SESSION_SCOPE, GLOBAL_SESSION_SCOPE, REQUEST_SCOPE, BUNDLE_SCOPE};
  }
}