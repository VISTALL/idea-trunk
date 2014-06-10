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
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.UmlCategory;
import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.settings.UmlLayout;

import java.awt.geom.Point2D;

/**
 * @author Konstantin Bulenkov
 */
public interface UmlPresentation extends FileEditorState {
  String getProviderID();
  void setProviderID(String id);

  boolean isCamel();
  void setCamel(boolean camel);

  boolean isColorManagerEnabled();
  void setColorManagerEnabled(boolean enabled);

  boolean isShowDependencies();
  void setShowDependencies(boolean show);

  boolean isEdgeCreationMode();
  void setEdgeCreationMode(boolean enable);

  void setVcsFilterEnabled(boolean enabled);
  boolean isVcsFilterEnabled();

  UmlLayout getLayout();
  void setLayout(UmlLayout layout);

  boolean isFitContentAfterLayout();
  void setFitContentAfterLayout(boolean enabled);

  boolean isCategoryEnabled(UmlCategory category);
  void setCategoryEnabled(UmlCategory category, boolean enabled);
  UmlCategory[] getEnabledCategories();

  double getZoom();
  void setZoom(double zoom);

  void update(GraphBuilder<UmlNode, UmlEdge> builder);
  String[] getFQNs();

  String getNodeX(String fqn);
  String getNodeY(String fqn);

  String[] getSelectedNodes();

  Point2D getCenter();
  void setCenter(Point2D point);

  EdgeInfo getEdgeInfo(String src, String trg);

  String getOriginalFQN();
  void setOriginalFQN(String originalFQN);
}
