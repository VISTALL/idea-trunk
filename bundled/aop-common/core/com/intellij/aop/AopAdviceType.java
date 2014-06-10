/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.jam.AopConstants;

import javax.swing.*;

/**
 * @author peter
 */
public enum AopAdviceType {
  BEFORE(AopConstants.BEFORE_ADVICE_ICON, true),
  AFTER(AopConstants.AFTER_ADVICE_ICON, false),
  AFTER_RETURNING(AopConstants.AFTER_RETURNING_ADVICE_ICON, false),
  AFTER_THROWING(AopConstants.AFTER_THROWING_ADVICE_ICON, false),
  AROUND(AopConstants.AROUND_ADVICE_ICON, true);

  private final Icon myIcon;
  private final boolean myOnTheWayIn;

  private AopAdviceType(final Icon icon, boolean onTheWayIn) {
    myIcon = icon;
    myOnTheWayIn = onTheWayIn;
  }

  public Icon getAdviceIcon() {
    return myIcon;
  }

  public boolean isOnTheWayIn() {
    return myOnTheWayIn;
  }
}
