package com.intellij.seam.model.xml.core;

/**
 * http://jboss.com/products/seam/core:transaction-management-enabledAttrType enumeration.
 */
public enum TransactionManagementEnabled implements com.intellij.util.xml.NamedEnum {
  FALSE("false"),
  TRUE("true");

  private final String value;

  private TransactionManagementEnabled(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
