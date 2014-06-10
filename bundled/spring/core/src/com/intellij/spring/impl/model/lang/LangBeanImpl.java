/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.lang;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.lang.LangBean;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public abstract class LangBeanImpl extends DomSpringBeanImpl implements LangBean {

  @Nullable
  public String getClassName() {
    return null;
  }
}
