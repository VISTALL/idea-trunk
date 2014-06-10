/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * @author Gregory.Shrago
 */
public enum ReleaseConnectionsType implements NamedEnum {
  AUTO("auto"), ON_CLOSE("on_close"), AFTER_TRANSACTION("after_transaction"), AFTER_STATEMENT("after_statement");

  private final String myValue;

  public String getValue() {
    return myValue;
  }

  ReleaseConnectionsType(@NonNls final String value) {
    myValue = value;
  }
}