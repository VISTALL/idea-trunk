package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum NotFoundType implements NamedEnum {
  IGNORE("ignore"), EXCEPTION("exception");

  private final String value;

  private NotFoundType(@NonNls String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
