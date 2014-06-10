package com.intellij.spring.impl.model.jee;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.jee.LocalSlsb;
import org.jetbrains.annotations.NotNull;

public abstract class LocalSlsbImpl extends DomSpringBeanImpl implements LocalSlsb {

  @NotNull
  public String getClassName() {
    return "org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean";
  }
}