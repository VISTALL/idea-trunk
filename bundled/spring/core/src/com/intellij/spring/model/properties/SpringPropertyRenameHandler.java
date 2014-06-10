/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.properties;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.refactoring.rename.BeanPropertyRenameHandler;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringPropertyRenameHandler extends BeanPropertyRenameHandler {
  @Nullable
  protected BeanProperty getProperty(DataContext context) {
    return SpringPropertiesUtil.getBeanProperty(context);
  }
}
