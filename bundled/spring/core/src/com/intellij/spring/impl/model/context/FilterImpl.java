package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.context.Filter;
import org.jetbrains.annotations.Nullable;

public abstract class FilterImpl extends DomSpringBeanImpl implements Filter {

  @Nullable
  public String getClassName() {
    return null; //todo
  }
}
