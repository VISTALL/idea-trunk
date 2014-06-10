/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public interface SpringConfigurator {

  ExtensionPointName<SpringConfigurator> EP_NAME = new ExtensionPointName<SpringConfigurator>("com.intellij.spring.configurator");

  /**
   * Configures Spring for given module
   *
   * @param module
   * @return true if Spring successfully configured
   */
  boolean configure(@NotNull Module module);
}
