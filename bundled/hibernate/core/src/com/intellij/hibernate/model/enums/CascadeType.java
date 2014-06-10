package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum CascadeType implements NamedEnum {
  PERSIST("persist"),
  CREARTE("create"),
  MERGE("merge"),
  DELETE("delete"),
  SAVE_UPDATE("save-update"),
  EVICT("evict"),
  REPLICATE("replicate"),
  LOCK("lock"),
  REFRESH("refresh"),

  DELETE_ORPHAN("delete-orphan"),
  ALL_DELETE_ORPHAN("all-delete-orphan"),
  ALL("all"),
  NONE("none");

  private final String value;

  private CascadeType(@NonNls String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return getValue();
  }

}
