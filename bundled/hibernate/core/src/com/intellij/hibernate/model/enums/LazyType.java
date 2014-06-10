/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.enums;

import com.intellij.javaee.model.xml.persistence.mapping.FetchType;
import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum LazyType implements NamedEnum {
  TRUE("true"), FALSE("false"), EXTRA("extra"), PROXY("proxy"), NO_PROXY("no-proxy");

  private final String myValue;

  private LazyType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

  public String getDisplayName() {
    switch (this) {
      case FALSE: return FetchType.EAGER.getDisplayName();
      case TRUE: return FetchType.LAZY.getDisplayName();
      default: return getValue();
    }
  }

}