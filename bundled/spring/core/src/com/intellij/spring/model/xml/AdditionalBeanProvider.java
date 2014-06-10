/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author peter
 */
public interface AdditionalBeanProvider<T extends DomElement> {
  @Nullable
  List<CommonSpringBean> getAdditionalBeans(@NotNull final T t, @NotNull Module module);
}
