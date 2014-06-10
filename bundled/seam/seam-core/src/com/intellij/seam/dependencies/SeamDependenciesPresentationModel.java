package com.intellij.seam.dependencies;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.components.SelectionDependenciesPresentationModel;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.OrientationLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicGroupLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.module.Module;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.seam.dependencies.beans.SeamComponentNodeInfo;
import com.intellij.seam.dependencies.beans.SeamDependencyInfo;
import com.intellij.seam.dependencies.renderers.DefaultSeamComponentRenderer;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SeamDependenciesPresentationModel extends SelectionDependenciesPresentationModel<SeamComponentNodeInfo, SeamDependencyInfo> {
  private final Module myProject;
  private BasicGraphNodeRenderer myRenderer;

  public SeamDependenciesPresentationModel(final Graph2D graph, Module module) {
    super(graph);
    myProject = module;
    setShowEdgeLabels(true);

    customizeDefaultSettings(GraphSettingsProvider.getInstance(module.getProject()).getSettings(graph));
  }

  private static void customizeDefaultSettings(final GraphSettings settings) {
    final HierarchicGroupLayouter groupLayouter = settings.getGroupLayouter();

    groupLayouter.setOrientationLayouter(GraphManager.getGraphManager().createOrientationLayouter(OrientationLayouter.TOP_TO_BOTTOM));
    groupLayouter.setMinimalNodeDistance(20);
    groupLayouter.setMinimalLayerDistance(50);
    groupLayouter.setRoutingStyle(HierarchicLayouter.ROUTE_POLYLINE);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final SeamComponentNodeInfo node) {
    return GraphViewUtil.createNodeRealizer("SeamComponentNodeInfoRenderer", getRenderer());
  }

  public BasicGraphNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new DefaultSeamComponentRenderer(getGraphBuilder());
    }
    return myRenderer;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(final SeamDependencyInfo edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();

    edgeRealizer.setLineType(LineType.LINE_1);
    edgeRealizer.setLineColor(Color.GRAY);
    edgeRealizer.setArrow(Arrow.STANDARD);

    return edgeRealizer;
  }

  public boolean editNode(final SeamComponentNodeInfo node) {
    final PsiElement psiElement = node.getIdentifyingElement().getIdentifyingPsiElement();
    if (psiElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)psiElement}, true);
      return true;
    }
    return super.editNode(node);
  }

  public boolean editEdge(final SeamDependencyInfo info) {
    final PsiElement psiElement = info.getIdentifyingElement().getIdentifyingPsiElement();
    if (psiElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)psiElement}, true);
      return true;
    }
    return super.editEdge(info);
  }

  public String getNodeTooltip(final SeamComponentNodeInfo node) {
    return node.getName();
  }

  public String getEdgeTooltip(final SeamDependencyInfo edge) {
    return edge.getName();
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowEdgeCreation(false);
    editMode.allowBendCreation(false);

    view.setFitContentOnResize(false);
    view.fitContent();
  }

  public NodeCellEditor getCustomNodeCellEditor(final SeamComponentNodeInfo SeamComponentNodeInfo) {
    return null;
  }

  public DefaultActionGroup getNodeActionGroup(final SeamComponentNodeInfo info) {
    return super.getNodeActionGroup(info);
  }

}
