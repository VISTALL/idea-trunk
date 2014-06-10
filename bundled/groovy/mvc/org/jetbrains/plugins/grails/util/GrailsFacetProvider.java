/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package org.jetbrains.plugins.grails.util;

import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.extensions.ExtensionPointName;

/**
 * @author peter
 */
public interface GrailsFacetProvider {
  ExtensionPointName<GrailsFacetProvider> EP_NAME = ExtensionPointName.create("org.intellij.groovy.mvc.grails.facetProvider");

  void addFacets(ModifiableFacetModel facetModel, Module module, VirtualFile root);

}
