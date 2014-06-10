package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum CacheIncludeType implements NamedEnum {
  ALL("all"), NON_LAZY("non-lazy");

  private final String myValue;

  private CacheIncludeType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }
}
