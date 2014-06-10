package com.intellij.seam.model.xml.pageflow;

import com.intellij.util.xml.NamedEnum;

public enum Enabled implements NamedEnum {
  DISABLED("disabled"),
  ENABLED("enabled");

  private final String value;

  private Enabled(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
