package com.intellij.spring.model.xml.jms;

import com.intellij.util.xml.NamedEnum;

public enum Acknowledge implements NamedEnum {
  AUTO("auto"),
  CLIENT("client"),
  DUPS_OK("dups-ok"),
  TRANSACTED("transacted");

  private final String value;

  private Acknowledge(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
