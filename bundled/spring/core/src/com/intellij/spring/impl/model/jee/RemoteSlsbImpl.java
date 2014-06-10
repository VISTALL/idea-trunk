package com.intellij.spring.impl.model.jee;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.jee.RemoteSlsb;
import org.jetbrains.annotations.NotNull;

public abstract class RemoteSlsbImpl extends DomSpringBeanImpl implements RemoteSlsb {

  @NotNull
  public String getClassName() {
    return "org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean";
  }
}