/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package org.jetbrains.plugins.grails.util;

import com.intellij.facet.ModifiableFacetModel;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetConfigurationImpl;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;

/**
 * @author peter
 */
public class GrailsWebFacetProvider implements GrailsFacetProvider {
  private static final String GRAILS_WEB_FACET = "GrailsWeb";

  public void addFacets(ModifiableFacetModel facetModel, Module module, VirtualFile root) {
    final VirtualFile webApp = root.findFileByRelativePath("web-app");
    if (webApp == null) return;

    final Collection<WebFacet> facetCollection = WebFacet.getInstances(module);
    WebFacet facet;
    if (!facetCollection.isEmpty()) {
      facet = facetCollection.iterator().next();
    }
    else {
      final WebFacetConfigurationImpl configuration = (WebFacetConfigurationImpl)WebFacetType.INSTANCE.createDefaultConfiguration();
      facetModel.addFacet(facet = WebFacetType.INSTANCE.createFacet(module, GRAILS_WEB_FACET, configuration, null));
    }

    facet.addWebRoot(webApp, "/");
  }
}
