package com.intellij.seam.model.xml.core;

/**
 * http://jboss.com/products/seam/core:debugAttrType enumeration.
 */
public enum Debug implements com.intellij.util.xml.NamedEnum {
  FALSE("false"),
  TRUE("true");

  private final String value;

  private Debug(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
