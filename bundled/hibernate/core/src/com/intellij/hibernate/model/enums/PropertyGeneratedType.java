package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum PropertyGeneratedType implements NamedEnum {
  NEVER("never"), INSERT("insert"), ALWAYS("always");

  private final String myValue;

  private PropertyGeneratedType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
