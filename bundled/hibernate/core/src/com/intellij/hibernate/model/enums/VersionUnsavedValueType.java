package com.intellij.hibernate.model.enums;

import org.jetbrains.annotations.NonNls;
import com.intellij.util.xml.NamedEnum;

public enum VersionUnsavedValueType implements NamedEnum {
  NULL("null"), NEGATIVE("negative"), UNDEFINED("undefined");

  private final String myValue;

  private VersionUnsavedValueType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
