package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum LockModeType implements NamedEnum {
  FORCE("force"), NONE("none"), READ("read"), UPGRADE("upgrade"),
  UPDADE_NOWAIT("upgrade_nowait"), WAIT("wait");

  private final String myValue;

  private LockModeType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
