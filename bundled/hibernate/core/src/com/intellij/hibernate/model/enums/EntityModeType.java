package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * @author Gregory.Shrago
 */
public enum EntityModeType implements NamedEnum {
  DYNAMIC_MAP("dynamic-map"), DOM4J("dom4j"), POJO("pojo");

  private final String myValue;

  public String getValue() {
    return myValue;
  }

  EntityModeType(@NonNls final String value) {
    myValue = value;
  }
}