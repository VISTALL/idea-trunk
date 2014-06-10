package com.intellij.seam.model.xml.theme;

/**
 * http://jboss.com/products/seam/theme:cookie-enabledAttrType enumeration.
 */
public enum CookieEnabled implements com.intellij.util.xml.NamedEnum {
  FALSE("false"),
  TRUE("true");

  private final String value;

  private CookieEnabled(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
