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

package com.intellij.uml;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.EdgeLayout;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.model.UmlPsiElementNode;
import com.intellij.uml.model.UmlRelationship;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.uml.settings.UmlConfiguration;
import com.intellij.uml.settings.UmlLayout;
import com.intellij.uml.settings.UmlSettings;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public class UmlDiagramState implements FileEditorState, UmlDiagramPresentation {
  private boolean showCamel = false;
  private boolean showFields = false;
  private boolean showConstructors = false;
  private boolean showMethods = false;
  private String myHighlightedPackage = null;
  private boolean showColors = true;
  private boolean showDependencies = false;
  private boolean showInnerClasses = false;
  private boolean enableEdgeCreation = false;
  private boolean vcsFilterEnabled = false;
  private UmlLayout layout = UmlLayout.getDefault();
  private boolean fitContentAfterLayout = false;
  private VisibilityLevel visLevel = VisibilityLevel.PRIVATE;
  final List<String> classes = new ArrayList<String>();
  private final Map<String, Pair<Double, Double>> nodesCoord = new HashMap<String, Pair<Double, Double>>();
  private final Map<Pair<String, String>, EdgeInfo> edgesCoord = new HashMap<Pair<String, String>, EdgeInfo>();
  final List<String> packages = new ArrayList<String>();
  private boolean showProperties = false;
  private final List<String> selectedNodes = new ArrayList<String>();
  private double zoom = 1.0;
  private Point2D location = new Point(0,0);

  public UmlDiagramState(UmlConfiguration configuration) {
    showCamel = configuration.showCamelNames;
    showFields = configuration.showFields;
    showConstructors = configuration.showConstructors;
    showMethods = configuration.showMethods;
    showDependencies = configuration.showDependencies;
    showInnerClasses = configuration.showInnerClasses;
    showProperties = configuration.showProperties;
    visLevel = configuration.visibilityLevel;
    vcsFilterEnabled = configuration.showChanges;
    layout = configuration.layout;
    showColors = configuration.showColors;
    fitContentAfterLayout = configuration.fitContentAfterLayout;
  }

  public UmlDiagramState() {}

  public boolean canBeMergedWith(final FileEditorState otherState, final FileEditorStateLevel level) {
    return false;
  }

  public void update(GraphBuilder<UmlNode, UmlEdge> builder) {
    classes.clear();
    selectedNodes.clear();
    nodesCoord.clear();
    edgesCoord.clear();
    packages.clear();

    final Map<UmlNode, String> fqnCache = new HashMap<UmlNode, String>();

    for (UmlNode node : builder.getNodeObjects()) {
      final PsiElement element = node.getIdentifyingElement();
      String fqn = null;
      if (element instanceof PsiClass) {
        fqn = ((PsiClass)element).getQualifiedName();
        classes.add(fqn);
      } else if (element instanceof PsiPackage) {
        fqn = ((PsiPackage)element).getQualifiedName();
        packages.add(fqn);
      }
      if (fqn != null) {
        fqnCache.put(node, fqn);
        final YPoint point = builder.getGraph().getLocation(builder.getNode(node));
        nodesCoord.put(fqn, new Pair<Double, Double>(point.getX(), point.getY()));
        if (builder.getGraph().isSelected(builder.getNode(node))) {
          selectedNodes.add(fqn);
        }
      }
    }

    UmlDiagramPresentation presentation = UmlUtils.getPresentation(builder);
    showCamel = presentation.isCamel();
    showFields = presentation.isFieldsVisible();
    showConstructors = presentation.isConstructorsVisible();
    showMethods = presentation.isMethodsVisible();
    showDependencies = presentation.isShowDependencies();
    showProperties = presentation.isPropertiesVisible();
    showInnerClasses = presentation.isShowInnerClasses();
    myHighlightedPackage = presentation.getHighlightedPackage();
    showColors = presentation.isColorManagerEnabled();
    visLevel = presentation.getVisibilityLevel();
    zoom = builder.getView().getZoom();
    location = builder.getView().getCenter();
    vcsFilterEnabled = presentation.isVcsFilterEnabled();

    for (UmlEdge umlEdge : builder.getEdgeObjects()) {
      final Edge edge = builder.getEdge(umlEdge);
      final EdgeLayout edgeLayout = builder.getGraph().getEdgeLayout(edge);
      if (edgeLayout != null) {
        final EdgeInfo info = new EdgeInfo(umlEdge, builder);
        edgesCoord.put(new Pair<String, String>(info.getSrc(), info.getTrg()), info);
      }
    }
  }

  public List<String> getClasses() {
    return classes;
  }

  public List<String> getPackages() {
    return packages;
  }

  public boolean isCamel() {
    return showCamel;
  }

  public boolean isFieldsVisible() {
    return showFields;
  }

  public boolean isConstructorsVisible() {
    return showConstructors;
  }

  public boolean isMethodsVisible() {
    return showMethods;
  }

  public String getHighlightedPackage() {
    return myHighlightedPackage;
  }

  public boolean isColorManagerEnabled() {
    return showColors;
  }

  public boolean isShowDependencies() {
    return showDependencies;
  }

  public boolean isShowInnerClasses() {
    return showInnerClasses;
  }

  public void setFieldsVisible(final boolean visible) {
    showFields = visible;
  }

  public void setCamel(final boolean camel) {
    showCamel = camel;
  }

  public void setConstructorVisible(final boolean visible) {
    showConstructors = visible;
  }

  public void setMethodsVisible(final boolean visible) {
    showMethods = visible;
  }

  public void setHighlightedPackage(final String packageName) {
    myHighlightedPackage = packageName;
  }

  public void setColorManagerEnabled(final boolean enabled) {
    showColors = enabled;
  }

  public void setShowDependencies(final boolean show) {
    showDependencies = show;
  }

  public void setShowInnerClasses(final boolean visible) {
    showInnerClasses = visible;
  }

  public boolean isEdgeCreationMode() {
    return enableEdgeCreation;
  }

  public void setEdgeCreationMode(final boolean enable) {
    enableEdgeCreation = enable;
  }

  public void setVisibilityLevel(@NotNull VisibilityLevel level) {
    visLevel = level;
  }

  @NotNull
  public VisibilityLevel getVisibilityLevel() {
    return visLevel;
  }

  public void setPropertiesVisible(boolean visible) {
    showProperties = visible;
  }

  public boolean isPropertiesVisible() {
    return showProperties;
  }

  public void setVcsFilterEnabled(boolean enabled) {
    vcsFilterEnabled = enabled;
  }

  public boolean isVcsFilterEnabled() {
    return vcsFilterEnabled;
  }

  public UmlLayout getLayout() {
    return layout;
  }

  public void setLayout(UmlLayout layout) {
    this.layout = layout;
  }

  public boolean isFitContentAfterLayout() {
    return fitContentAfterLayout;
  }

  public void setFitContentAfterLayout(boolean enabled) {
    fitContentAfterLayout = enabled;
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public String getNodeX(String fqn) {
    final Pair<Double, Double> coord = nodesCoord.get(fqn);
    return coord == null ? "" : String.valueOf(coord.getFirst());
  }

  public String getNodeY(String fqn) {
    final Pair<Double, Double> coord = nodesCoord.get(fqn);
    return coord == null ? "" : String.valueOf(coord.getSecond());
  }

  public void setNodeCoord(String fqn, double x, double y) {
    nodesCoord.put(fqn, new Pair<Double, Double>(x,y));
  }

  public Collection<EdgeInfo> getEdgesCoord() {
    return edgesCoord.values();
  }

  @Nullable
  public EdgeInfo getEdgeInfo(String src, String trg) {
    return edgesCoord.get(new Pair<String, String>(src, trg));
  }

  public void setEdgesCoord(@NotNull String srcFqn, @NotNull String trgFqn, @NotNull UmlRelationship relationship, @NotNull List<Pair<Double, Double>> edgeCoord) {
    edgesCoord.put(new Pair<String, String>(srcFqn, trgFqn), new EdgeInfo(srcFqn, trgFqn, relationship, edgeCoord));
  }

  public Point2D getCenter() {
    return location;
  }

  public void setCenter(Point2D point) {
    location = point;
  }

  public List<String> getSelectedNodes() {
    return selectedNodes;
  }

  public void setSelectedNodes(List<String> nodes) {
    selectedNodes.clear();
    selectedNodes.addAll(nodes);
  }

  public static class EdgeInfo {
    private final UmlRelationship relationship;
    private final String src;
    private final String trg;
    private final List<Pair<Double, Double>> points;
    public EdgeInfo(UmlEdge edge, GraphBuilder<UmlNode, UmlEdge> builder) {
      relationship = edge.getRelationship();
      src = ((UmlPsiElementNode)edge.getSource()).getFQN();
      trg = ((UmlPsiElementNode)edge.getTarget()).getFQN();      
      final EdgeLayout edgeLayout = builder.getGraph().getEdgeLayout(builder.getEdge(edge));
      final YPoint sp = edgeLayout.getSourcePoint();
      points = new ArrayList<Pair<Double, Double>>();
      points.add(new Pair<Double, Double>(sp.getX(), sp.getY()));
      for (int i = 0; i < edgeLayout.pointCount(); i++) {
        final YPoint p = edgeLayout.getPoint(i);
        points.add(new Pair<Double, Double>(p.getX(), p.getY()));
      }
      final YPoint tp = edgeLayout.getTargetPoint();
      points.add(new Pair<Double, Double>(tp.getX(), tp.getY()));
    }

    public EdgeInfo(String src, String trg, UmlRelationship relationship, List<Pair<Double, Double>> points) {
      this.src = src;
      this.trg = trg;
      this.relationship = relationship;
      this.points = points;
    }

    public UmlRelationship getRelationship() {
      return relationship;
    }

    public String getSrc() {
      return src;
    }

    public String getTrg() {
      return trg;
    }

    public List<Pair<Double, Double>> getPoints() {
      return points;
    }
  }

  public static UmlDiagramState getDefault() {
    return UmlSettings.getInstance().getDefaultState();
  }
}
