package com.intellij.spring.impl.model.util;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.util.UtilProperties;
import org.jetbrains.annotations.NotNull;

public abstract class UtilPropertiesImpl extends DomSpringBeanImpl implements UtilProperties {
  @NotNull
  public String getClassName() {
    return BEAN_CLASS_NAME;
  }
}
