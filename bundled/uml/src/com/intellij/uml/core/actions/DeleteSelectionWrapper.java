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

package com.intellij.uml.core.actions;

import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.Utils;
import com.intellij.uml.UmlDataKeys;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class DeleteSelectionWrapper extends GraphActionWrapper {
  public DeleteSelectionWrapper(@NotNull AbstractAction action, @NotNull GraphBuilder<UmlNode, UmlEdge> builder) {
    super(action, builder);
  }

  public void actionPerformed(final ActionEvent e) {
    final List<Node> selectedNodes = GraphViewUtil.getSelectedNodes(getBuilder().getGraph());
    final List<Edge> selectedEdges = GraphViewUtil.getSelectedEdges(getBuilder().getGraph());
    if (selectedEdges.size() == 1 && selectedNodes.size() == 0) {
      final UmlEdge edge = getBuilder().getEdgeObject(selectedEdges.get(0));

      if (edge == null) return;

      Utils.getDataModel(getBuilder()).removeEdge(edge);

    } else {
      for (Node node : selectedNodes) {
        UmlNode umlNode = getBuilder().getNodeObject(node);
        if (umlNode == null) continue;
        Utils.getDataModel(getBuilder()).removeNode(umlNode);
      }
    }
    final boolean updateLayout = Utils.isPopupMode(getBuilder()) && !selectedNodes.isEmpty();
    Utils.updateGraph(getBuilder(), updateLayout, updateLayout);
    if (updateLayout) {
      final JBPopup popup = getBuilder().getUserData(UmlDataKeys.UML_POPUP);
      if (popup != null) {
        getBuilder().getView().updateView();
        GraphUtil.setBestPopupSizeForGraph(popup, getBuilder());
      }
    }
  }
}
