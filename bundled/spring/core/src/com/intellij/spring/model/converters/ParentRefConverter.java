/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Dmitry Avdeev
*/
public class ParentRefConverter extends SpringBeanResolveConverter.PropertyBean {

  public SpringBeanPointer fromString(final @Nullable String s, final ConvertContext context) {
    if (s == null) return null;
    final SpringModel model = getSpringModel(context);
    return model == null ? null : model.findParentBean(s);
  }

  @NotNull
  public Collection<SpringBeanPointer> getVariants(final ConvertContext context) {
    return getVariants(context, true);
  }
}
