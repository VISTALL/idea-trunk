/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package org.jetbrains.plugins.grails.util;

import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.Condition;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public class GrailsSpringFacetProvider implements GrailsFacetProvider {
  @NonNls private static final String GRAILS_FILESET = "Grails";

  public void addFacets(ModifiableFacetModel facetModel, Module module, VirtualFile root) {
    final VirtualFile appContext = root.findFileByRelativePath("web-app/WEB-INF/applicationContext.xml");
    if (appContext == null) return;

    SpringFacet facet = SpringFacet.getInstance(module);
    if (facet == null) {
      facetModel.addFacet(facet = SpringFacetType.INSTANCE.createFacet(module, "Spring", SpringFacetType.INSTANCE.createDefaultConfiguration(), null));
    }

    final SpringFacetConfiguration configuration = facet.getConfiguration();
    SpringFileSet fileSet = ContainerUtil.find(configuration.getFileSets(), new Condition<SpringFileSet>() {
      public boolean value(SpringFileSet springFileSet) {
        return GRAILS_FILESET.equals(springFileSet.getId());
      }
    });
    if (fileSet == null) {
      configuration.getFileSets().add(fileSet = new SpringFileSet(GRAILS_FILESET, GRAILS_FILESET, configuration));
    }
    fileSet.addFile(appContext);
  }
}