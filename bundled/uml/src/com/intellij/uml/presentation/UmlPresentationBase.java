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

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.EdgeLayout;
import com.intellij.openapi.util.Pair;
import com.intellij.uml.*;
import com.intellij.uml.settings.UmlLayout;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPresentationBase implements UmlPresentation {
  private final List<UmlCategory> enabledCategories = new ArrayList<UmlCategory>();
  private boolean isCamelEnabled = false;
  private boolean isColorEnabled = true;
  private boolean isDepenciesEnabled = false;
  private boolean isEdgeCreationMode = false;
  private boolean isVcsMode = false;
  private boolean isFitContentAfterLayout = false;
  private final UmlPresentationModel presentationModel;
  private UmlLayout layout = UmlLayout.HIERARCHIC_GROUP;
  private Set<String> fqns = new HashSet<String>();
  private final Map<String, Pair<Double, Double>> nodesCoord = new HashMap<String, Pair<Double, Double>>();
  private final Map<Pair<String, String>, EdgeInfo> edgesCoord = new HashMap<Pair<String, String>, EdgeInfo>();
  private final Set<String> selectedNodes = new HashSet<String>();
  private double zoom = 1.0;
  private Point2D location = new Point(0,0);
  private String providerID;
  private String originalFQN;

  public UmlPresentationBase(UmlPresentationModel model, UmlProvider provider) {
    this.presentationModel = model;
    providerID = provider == null ? null : provider.getID();    
  }

  public UmlPresentationBase() {
    this(null, null);
  }

  public void copyFrom(@NotNull UmlPresentation presentation) {
    final UmlPresentationBase base = (UmlPresentationBase)presentation;
    enabledCategories.clear();
    enabledCategories.addAll(base.enabledCategories);
    isCamelEnabled = base.isCamelEnabled;
    isColorEnabled = base.isColorEnabled;
    isDepenciesEnabled = base.isDepenciesEnabled;
    isEdgeCreationMode = base.isEdgeCreationMode;
    layout = base.layout;
    isFitContentAfterLayout = base.isFitContentAfterLayout;
    isVcsMode = base.isVcsMode;
    selectedNodes.clear();
    selectedNodes.addAll(base.selectedNodes);
    nodesCoord.clear();
    nodesCoord.putAll(base.nodesCoord);
    fqns.clear();
    fqns.addAll(base.fqns);
    edgesCoord.clear();
    edgesCoord.putAll(base.edgesCoord);
    location = base.location;
    zoom = base.zoom;
  }

  public String getProviderID() {
    return providerID;
  }

  public void setProviderID(String id) {
    providerID = id;
  }

  public boolean isCamel() {
    return isCamelEnabled;
  }

  public void setCamel(boolean camel) {
    if (isCamelEnabled != camel) {
      isCamelEnabled = camel;
      update();
    }
  }

  private void update() {
    if (presentationModel != null) {
      presentationModel.update();
    }
  }

  public boolean isColorManagerEnabled() {
    return isColorEnabled;
  }

  public void setColorManagerEnabled(boolean enabled) {
    if (isColorEnabled != enabled) {
      isColorEnabled = enabled;
      update();
    }
  }

  public boolean isShowDependencies() {
    return isDepenciesEnabled;
  }

  public void setShowDependencies(boolean enabled) {
    if (isDepenciesEnabled != enabled) {
      isDepenciesEnabled = enabled;
      update();
    }
  }

  public boolean isEdgeCreationMode() {
    return isEdgeCreationMode;
  }

  public void setEdgeCreationMode(boolean enable) {
    if (isEdgeCreationMode != enable) {
      isEdgeCreationMode = enable;
      update();
    }
  }

  public void setVcsFilterEnabled(boolean enabled) {
    if (isVcsMode != enabled) {
      isVcsMode = enabled;
      update();
    }
  }

  @Nullable
  public EdgeInfo getEdgeInfo(String src, String trg) {
    return edgesCoord.get(new Pair<String, String>(src, trg));
  }

  public boolean isVcsFilterEnabled() {
    return isVcsMode;
  }

  public UmlLayout getLayout() {
    return layout;
  }

  public void setLayout(UmlLayout layout) {
    if (this.layout != layout) {
      this.layout = layout;
      update();
    }
  }

  public boolean isFitContentAfterLayout() {
    return isFitContentAfterLayout;
  }

  public void setFitContentAfterLayout(boolean enabled) {
  }

  public boolean isCategoryEnabled(UmlCategory category) {
    return enabledCategories.contains(category);
  }

  public void setCategoryEnabled(UmlCategory category, boolean enabled) {
    if (enabled) {
      if (!enabledCategories.contains(category)) {
        enabledCategories.add(category);
      }
    } else {
      enabledCategories.remove(category);
    }
  }

  public UmlCategory[] getEnabledCategories() {
    return enabledCategories.toArray(new UmlCategory[enabledCategories.size()]);
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public void update(GraphBuilder<UmlNode, UmlEdge> builder) {
    fqns.clear();
    selectedNodes.clear();
    nodesCoord.clear();
    edgesCoord.clear();
    zoom = builder.getView().getZoom();
    location = builder.getView().getCenter();

    final UmlDataModel<?> model = Utils.getDataModel(builder);
    final UmlProvider umlProvider = Utils.getProvider(builder);
    final UmlVfsResolver resolver = umlProvider.getVfsResolver();
    providerID = umlProvider.getID();

    for (UmlNode node : model.getNodes()) {
      final String fqn = resolver.getQualifiedName(node.getIdentifyingElement());
      if (fqn != null) {
        fqns.add(fqn);
        final YPoint point = builder.getGraph().getLocation(builder.getNode(node));
        nodesCoord.put(fqn, new Pair<Double, Double>(point.getX(), point.getY()));
        if (builder.getGraph().isSelected(builder.getNode(node))) {
          selectedNodes.add(fqn);
        }
      }
    }

    for (UmlEdge umlEdge : builder.getEdgeObjects()) {
      final Edge edge = builder.getEdge(umlEdge);
      final EdgeLayout edgeLayout = builder.getGraph().getEdgeLayout(edge);
      if (edgeLayout != null) {
        final EdgeInfo info = new EdgeInfo(umlEdge, builder);
        edgesCoord.put(new Pair<String, String>(info.getSrc(), info.getTrg()), info);
      }
    }
  }

  public String[] getFQNs() {
    return fqns.toArray(new String[fqns.size()]);
  }

  public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
    return false;
  }

  public String getNodeX(String fqn) {
    final Pair<Double, Double> coord = nodesCoord.get(fqn);
    return coord == null ? "" : String.valueOf(coord.getFirst());
  }

  public String getNodeY(String fqn) {
    final Pair<Double, Double> coord = nodesCoord.get(fqn);
    return coord == null ? "" : String.valueOf(coord.getSecond());
  }

  public String[] getSelectedNodes() {
    return selectedNodes.toArray(new String[selectedNodes.size()]);
  }

  public Point2D getCenter() {
    return location;
  }

  public void setCenter(Point2D point) {
    location = point;
  }

  public void addFQN(String fqn) {
    fqns.add(fqn);
  }

  public void setNodeCoord(String fqn, double x, double y) {
    nodesCoord.put(fqn, new Pair<Double, Double>(x, y));
  }

  public void setSelectedNodes(List<String> selNodes) {
    selectedNodes.clear();
    selectedNodes.addAll(selNodes);
  }

  public void setCategories(Set<UmlCategory> cats) {
    enabledCategories.clear();
    enabledCategories.addAll(cats);
  }

  public EdgeInfo[] getEdgeInfos() {
    final Collection<EdgeInfo> edgeInfos = edgesCoord.values();
    return edgeInfos.toArray(new EdgeInfo[edgeInfos.size()]);
  }

  public void setEdgesCoord(@NotNull String srcFqn, @NotNull String trgFqn, @NotNull List<Pair<Double, Double>> edgeCoord) {
    edgesCoord.put(new Pair<String, String>(srcFqn, trgFqn), new EdgeInfo(srcFqn, trgFqn, edgeCoord));
  }

  public String getOriginalFQN() {
    return originalFQN;
  }

  public void setOriginalFQN(String originalFQN) {
    this.originalFQN = originalFQN;
  }
}
