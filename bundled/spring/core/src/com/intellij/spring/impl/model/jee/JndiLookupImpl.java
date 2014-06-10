package com.intellij.spring.impl.model.jee;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.xml.jee.JndiLookup;
import org.jetbrains.annotations.NotNull;

public abstract class JndiLookupImpl extends DomSpringBeanImpl implements JndiLookup {

  @NotNull
  public String getClassName() {
    return "org.springframework.jndi.JndiObjectFactoryBean";
  }
}