package com.intellij.hibernate.model.enums;

import org.jetbrains.annotations.NonNls;
import com.intellij.util.xml.NamedEnum;

public enum GeneratedType implements NamedEnum {
  NEVER("never"), ALWAYS("always");

  private final String myValue;

  private GeneratedType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
