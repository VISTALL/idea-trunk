/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories;

import com.intellij.spring.model.xml.CommonSpringBean;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ObjectTypeResolver {

  /**
   * @param context factory bean in container
   * @return class name of the object managed by this factory, or implemented interfaces. Like as {@link org.springframework.beans.factory.FactoryBean#getObjectType()}.
   */
  @NotNull
  Set<String> getObjectType(@NotNull CommonSpringBean context);

  boolean accept(@NotNull String factoryClassName);
}