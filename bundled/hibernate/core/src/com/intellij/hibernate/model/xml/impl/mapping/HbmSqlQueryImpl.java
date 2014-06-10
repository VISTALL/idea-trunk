/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.xml.mapping.HbmSqlQuery;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.util.xml.GenericValue;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmSqlQueryImpl extends BaseImpl implements HbmSqlQuery {

  public GenericValue<String> getQuery() {
    return this;
  }
}