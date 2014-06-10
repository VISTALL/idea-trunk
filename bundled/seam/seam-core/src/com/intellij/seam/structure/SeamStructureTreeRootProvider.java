package com.intellij.seam.structure;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.jam.view.tree.JamNodeDescriptor;
import com.intellij.jam.view.tree.JamTreeParameters;
import com.intellij.javaee.module.view.FacetHolderTreeRootProvider;
import com.intellij.openapi.project.Project;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.facet.SeamFacetType;

public class SeamStructureTreeRootProvider extends FacetHolderTreeRootProvider<SeamFacet> {
  public SeamStructureTreeRootProvider() {
    super(SeamFacetType.INSTANCE, 2);
  }

  public JamNodeDescriptor createFacetNodeDescriptor(final SeamFacet facet,
                                                     final JamNodeDescriptor parent,
                                                     final JamTreeParameters parameters) {
    return new SeamFacetNodeDescriptor(parent.getProject(), facet, parent, parameters);
  }

  public void initTreeBuilder(final Project project, final AbstractTreeBuilder builder) {
   new SeamTreeBuilder(project, builder);
  }
}
