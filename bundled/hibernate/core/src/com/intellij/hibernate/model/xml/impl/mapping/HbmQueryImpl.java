/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.hibernate.model.xml.mapping.HbmQuery;
import com.intellij.util.xml.GenericValue;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmQueryImpl extends BaseImpl implements HbmQuery {

  public GenericValue<String> getQuery() {
    return this;
  }
}
