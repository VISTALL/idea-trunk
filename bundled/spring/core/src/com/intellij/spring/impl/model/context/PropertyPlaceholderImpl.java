package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.values.PlaceholderUtils;
import com.intellij.spring.model.xml.context.PropertyPlaceholder;
import org.jetbrains.annotations.NotNull;

public abstract class PropertyPlaceholderImpl extends DomSpringBeanImpl implements PropertyPlaceholder {

  @NotNull
  public String getClassName() {
    return PlaceholderUtils.PLACEHOLDER_CONFIGURER_CLASS;
  }
}
