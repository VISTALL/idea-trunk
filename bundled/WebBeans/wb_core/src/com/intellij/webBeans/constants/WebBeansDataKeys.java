package com.intellij.webBeans.constants;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.webBeans.facet.WebBeansFacet;

public class WebBeansDataKeys {
  public static final DataKey<WebBeansFacet> WEB_BEANS_FACET = DataKey.create("WEB_BEANS_FACET");

  private WebBeansDataKeys() {
  }
}
