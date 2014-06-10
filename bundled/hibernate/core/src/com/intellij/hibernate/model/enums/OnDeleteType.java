package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum OnDeleteType implements NamedEnum {
  NOACTION("noaction"), CASCADE("cascade");

  private final String myValue;

  private OnDeleteType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }
}
