/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories.resolvers;

import com.intellij.spring.model.xml.CommonSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FactoryPropertiesDependentTypeResolver extends AbstractTypeResolver {
  private final List<String> myPropertyNames;

  public FactoryPropertiesDependentTypeResolver(final List<String> propertyNames) {
    myPropertyNames = propertyNames;
  }

  @NotNull
  public Set<String> getObjectType(@NotNull final CommonSpringBean context) {
    for (String propertyName : myPropertyNames) {
      String propertyValue = getPropertyValue(context, propertyName);
      if (propertyValue != null) {
        return Collections.singleton(propertyValue);
      }
    }
    return Collections.emptySet();
  }

  public boolean accept(@NotNull final String factoryClassName) {
    return true;
  }
}