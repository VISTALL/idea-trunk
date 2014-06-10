/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFileSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public interface SpringModelProvider {

  ExtensionPointName<SpringModelProvider> EP_NAME = new ExtensionPointName<SpringModelProvider>("com.intellij.spring.modelProvider");

  @NotNull
  List<SpringFileSet> getFilesets(@NotNull SpringFacet facet);
}
