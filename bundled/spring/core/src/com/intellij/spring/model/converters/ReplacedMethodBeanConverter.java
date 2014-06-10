/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.spring.model.highlighting.ReplacedMethodsInspection;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @see ReplacedMethodsInspection
 */
public class ReplacedMethodBeanConverter extends SpringBeanResolveConverterForDefiniteClasses {
  @NonNls public final static String METHOD_REPLACER_CLASS = "org.springframework.beans.factory.support.MethodReplacer";

  @NotNull
  protected String[] getClassNames(final ConvertContext context) {
    return new String[]{METHOD_REPLACER_CLASS};
  }
}