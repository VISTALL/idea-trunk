package com.intellij.spring.impl.model.util;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.util.PropertyPath;
import org.jetbrains.annotations.NotNull;

public abstract class UtilPropertyPathImpl extends DomSpringBeanImpl implements PropertyPath {
  @NotNull
  public String getClassName() {
    return "org.springframework.beans.factory.config.PropertyPathFactoryBean";
  }
}
