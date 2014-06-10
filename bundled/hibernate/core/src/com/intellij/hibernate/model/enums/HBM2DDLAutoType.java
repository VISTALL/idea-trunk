package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * @author Gregory.Shrago
 */
public enum HBM2DDLAutoType implements NamedEnum {
  VALIDATE("validate"), UPDATE("update"), CREATE("create"), CREATE_DROP("create-drop");

  private final String myValue;

  public String getValue() {
    return myValue;
  }

  HBM2DDLAutoType(@NonNls final String value) {
    myValue = value;
  }
}
