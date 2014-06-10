package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum CollectionFetchType implements NamedEnum {
  JOIN("join"), SELECT("select"), SUBSELECT("subselect");

  private final String myValue;

  private CollectionFetchType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }
}
