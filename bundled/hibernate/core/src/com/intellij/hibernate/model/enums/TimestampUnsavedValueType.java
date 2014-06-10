package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum TimestampUnsavedValueType implements NamedEnum {
  NULL("null"), UNDEFINED("undefined");
  private final String myValue;

  private TimestampUnsavedValueType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
