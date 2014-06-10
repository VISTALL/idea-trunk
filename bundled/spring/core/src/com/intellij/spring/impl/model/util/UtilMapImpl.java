package com.intellij.spring.impl.model.util;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.util.UtilMap;
import org.jetbrains.annotations.NotNull;

public abstract class UtilMapImpl extends DomSpringBeanImpl implements UtilMap {

  @NotNull
  public String getClassName() {
    return "org.springframework.beans.factory.config.MapFactoryBean";
  }
}