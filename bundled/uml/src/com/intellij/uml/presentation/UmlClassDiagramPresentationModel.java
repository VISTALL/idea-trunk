/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.uml.presentation;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.EdgeCreationPolicy;
import com.intellij.openapi.graph.builder.actions.layout.AbstractLayoutAction;
import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.OrientationLayouter;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicGroupLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.uml.actions.DefaultUmlActions;
import com.intellij.uml.editors.UmlPsiClassNodeCellEditor;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.renderers.DefaultUmlClassRenderer;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassDiagramPresentationModel extends BasicGraphPresentationModel<UmlNode, UmlEdge> {
  private final Project project;
  private DefaultUmlClassRenderer myRenderer;
  private long modificationCounter = 0;
  private final UmlDiagramPresentation myPresentation;
  private EditMode myEditMode;
  private boolean popupMode = false;

  public UmlClassDiagramPresentationModel(final Graph2D graph, Project project) {
    this(graph, project, null);
  }

  public UmlClassDiagramPresentationModel(final Graph2D graph, Project project, UmlDiagramPresentation presentation) {
    super(graph);
    myPresentation = new UmlDiagramPresentationImpl(this, presentation);
    this.project = project;
    setShowEdgeLabels(true);

    customizeDefaultSettings();
  }

  private void customizeDefaultSettings() {
    final Graph2D graph = (Graph2D)getGraph();
    final Layouter layouter = myPresentation.getLayout().getLayouter(graph, project);
    if (layouter instanceof HierarchicGroupLayouter) {
      HierarchicGroupLayouter groupLayouter = (HierarchicGroupLayouter)layouter;
      groupLayouter.setOrientationLayouter(GraphManager.getGraphManager().createOrientationLayouter(OrientationLayouter.BOTTOM_TO_TOP));
      groupLayouter.setMinimalNodeDistance(20);
      groupLayouter.setMinimalLayerDistance(50);
      groupLayouter.setRoutingStyle(HierarchicLayouter.ROUTE_ORTHOGONAL);
    }
    final GraphSettings settings = GraphSettingsProvider.getInstance(project).getSettings(graph);
    settings.setCurrentLayouter(layouter);
    settings.setFitContentAfterLayout(myPresentation.isFitContentAfterLayout());
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final UmlNode node) {
    return GraphViewUtil.createNodeRealizer("UmlNodeInfoRenderer", getRenderer());
  }

  public DefaultUmlClassRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new DefaultUmlClassRenderer(getGraphBuilder(), new ModificationTracker() {
        public long getModificationCount() {
          return PsiManager.getInstance(project).getModificationTracker()
            .getJavaStructureModificationCount() + modificationCounter;
        }
      });
    }
    return myRenderer;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(final UmlEdge edge) {
    final EdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();
    edgeRealizer.setLineColor(UmlColorManager.getInstance().getEdgeColor(edge, this));
    edgeRealizer.setLineType(edge.getLineType());
    edgeRealizer.setArrow(edge.getArrow());
    edgeRealizer.setSourceArrow(edge.getSourceArrow());
    return edgeRealizer;
  }

  @Override
  public EdgeLabel[] getEdgeLabels(final UmlEdge umlEdge, final String edgeName) {
    String label = umlEdge.getLabel();
    if (label != null && label.trim().length() > 0) {
      if (label.contains(":")) {
        EdgeLabel left = GraphManager.getGraphManager().createEdgeLabel(label.substring(0, label.indexOf(':')));
        EdgeLabel right = GraphManager.getGraphManager().createEdgeLabel(label.substring(label.indexOf(':') + 1));
        left.setModel(EdgeLabel.SIX_POS);
        left.setPosition(EdgeLabel.SHEAD);
        //left.setDistance(0.0D);
        right.setModel(EdgeLabel.SIX_POS);
        right.setPosition(EdgeLabel.THEAD);
        //right.setDistance(0.0D);
        return new EdgeLabel[]{left, right};
      } else {
        EdgeLabel edgeLabel = GraphManager.getGraphManager().createEdgeLabel(label);
        edgeLabel.setModel(EdgeLabel.CENTERED);
        edgeLabel.setPosition(EdgeLabel.CENTER);
        //edgeLabel.setDistance(0.0D);
        return new EdgeLabel[]{edgeLabel};
      }
    } else {
      return super.getEdgeLabels(umlEdge, edgeName);
    }
  }

  public boolean editNode(final UmlNode node) {
    if (popupMode) {
      final PsiElement psiElement = node.getIdentifyingElement();
      if (psiElement instanceof Navigatable) {
        OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)psiElement}, false);
        return true;
      }
    } else {
      return false;
    }
    return super.editNode(node);
  }

  public boolean editEdge(final UmlEdge info) {
    final PsiElement psiElement = info.getIdentifyingElement();
    if (psiElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)psiElement}, true);
      return true;
    }
    return super.editEdge(info);
  }

  public String getNodeTooltip(final UmlNode node) {
    return node.getName();
  }

  public String getEdgeTooltip(final UmlEdge edge) {
    return edge.getName();
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    super.customizeSettings(view, editMode);
    myEditMode = editMode;
    view.setAntialiasedPainting(true);    
    editMode.allowEdgeCreation(false);
    editMode.allowBendCreation(false);
    editMode.allowNodeEditing(true);    

    //Install actions
    DefaultUmlActions.registerCustomShortcuts(getGraphBuilder());
    DefaultUmlActions.install(getGraphBuilder());

    //View settings
    view.setFitContentOnResize(false);
    view.setGridVisible(false);
    view.fitContent();
    GraphSettings settings = GraphSettingsProvider.getInstance(project).getSettings(view.getGraph2D());    

    //Zoom without ctrl
    //Graph2DViewMouseWheelZoomListener mwzl = GraphManager.getGraphManager().createGraph2DViewMouseWheelZoomListener();
    //view.getCanvasComponent().addMouseWheelListener(mwzl);
    AbstractLayoutAction.doLayout(view, settings.getCurrentLayouter(), project);
  }

  public UmlDiagramPresentation getPresentation() {
    return myPresentation;
  }

  private static final EdgeCreationPolicy<UmlNode> EDGE_CREATION_POLICY = new EdgeCreationPolicy<UmlNode>() {
      public boolean acceptSource(@NotNull final UmlNode source) {
        if (! (source.getIdentifyingElement() instanceof PsiClass)) return false;
        final PsiClass psiClass = (PsiClass)source.getIdentifyingElement();
        final PsiFile file = psiClass.getContainingFile();
        final Project prj = psiClass.getProject();
        if (file == null) return false;
        final VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) return false;
        return ProjectRootManager.getInstance(prj).getFileIndex().isInSource(virtualFile);
      }

      public boolean acceptTarget(@NotNull final UmlNode target) {
        return target.getIdentifyingElement() instanceof PsiClass;
      }
    };
  @Override
  public EdgeCreationPolicy<UmlNode> getEdgeCreationPolicy() {
    return EDGE_CREATION_POLICY;
  }

  public NodeCellEditor getCustomNodeCellEditor(final UmlNode umlNode) {
    return (umlNode.getIdentifyingElement() instanceof PsiClass) ?
      new UmlPsiClassNodeCellEditor(getGraphBuilder()) : super.getCustomNodeCellEditor(umlNode);
  }

  public DefaultActionGroup getNodeActionGroup(final UmlNode info) {
    return getCommonActionGroup();
  }

  @Override
  protected DefaultActionGroup getCommonActionGroup() {
    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(ActionManager.getInstance().getAction("Uml.DefaultGraphPopup"));
    return actions;
  }

  public void update() {
    modificationCounter++;
    UmlUtils.updateGraph(getGraphBuilder(), false, false);
  }

  public void repaint() {
    getGraphBuilder().getView().getCanvasComponent().repaint();
  }

  public EditMode getEditMode() {
    return myEditMode;
  }

  @Override
  public boolean isAutoRotateLabels() {
    return true;
  }

  public boolean isPopupMode() {
    return popupMode;
  }

  public void setPopupMode(boolean popupMode) {
    this.popupMode = popupMode;
  }
}
