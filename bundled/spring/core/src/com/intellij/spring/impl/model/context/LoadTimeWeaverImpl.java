package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.context.LoadTimeWeaver;
import org.jetbrains.annotations.Nullable;

public abstract class LoadTimeWeaverImpl extends DomSpringBeanImpl implements LoadTimeWeaver {
  @Nullable
  public String getClassName() {
    return null; //todo
  }
}
