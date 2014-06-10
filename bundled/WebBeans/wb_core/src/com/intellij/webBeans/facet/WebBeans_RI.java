package com.intellij.webBeans.facet;

import org.jetbrains.annotations.NonNls;
import com.intellij.webBeans.constants.WebBeansCommonConstants;

public enum WebBeans_RI {

  @NonNls JBOSS("JBoss", WebBeansCommonConstants.JBOSS_FACET_DETECTION_CLASS),
  @NonNls APACHE("Apache", WebBeansCommonConstants.APACHE_FACET_DETECTION_CLASS);

  private final String myName;
  private String myFacetDetectionClass;

  WebBeans_RI(String name, String facetDetectionClass) {
    myName = name;
    myFacetDetectionClass = facetDetectionClass;
  }

  public String getName() {
    return myName;
  }

  public String getFacetDetectionClass() {
    return myFacetDetectionClass;
  }

  public String toString() {
    return myName;
  }
}
