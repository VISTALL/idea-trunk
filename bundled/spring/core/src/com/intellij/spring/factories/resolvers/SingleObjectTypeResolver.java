/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories.resolvers;

import com.intellij.spring.factories.ObjectTypeResolver;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SingleObjectTypeResolver implements ObjectTypeResolver {

  private final Set<String> myTypes;

  public SingleObjectTypeResolver(final String type) {
    myTypes = new HashSet<String>(StringUtil.split(type, ","));
  }

  @NotNull
  public Set<String> getObjectType(@NotNull final CommonSpringBean bean) {
    return myTypes;
  }

  public boolean accept(@NotNull final String factoryClassName) {
    return true;
  }
}