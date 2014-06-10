package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum IdUnsavedValueType implements NamedEnum {
  NUMBER("<$%^\">"), NULL("null"), ANY("any"), NONE("none"), UNDEFINED("undefined");
  private final String myValue;

  private IdUnsavedValueType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
