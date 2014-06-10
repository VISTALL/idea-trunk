package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum CacheUsageType implements NamedEnum {
  TRANSACTIONAL("transactional"), READ_WRITE("read-write"), NONSTRICT_READ_WRITE("nonstrict-read-write"), READ_ONLY("read-only");

  private final String myValue;

  private CacheUsageType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }
}
