/*
 * Copyright 2008 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.graph;

import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.EditMode;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.graph.beans.BasicStrutsEdge;
import com.intellij.struts2.graph.beans.BasicStrutsNode;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 * @author Sergey Vasiliev
 */
public class StrutsPresentationModel extends BasicGraphPresentationModel<BasicStrutsNode, BasicStrutsEdge> {

  private BasicGraphNodeRenderer myRenderer;

  public StrutsPresentationModel(final Graph2D graph) {
    super(graph);
    setShowEdgeLabels(true);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final BasicStrutsNode node) {
    return GraphViewUtil.createNodeRealizer("Struts2NodeRenderer", getRenderer());
  }

  private BasicGraphNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new StrutsNodeRenderer(getGraphBuilder());
    }
    return myRenderer;
  }

  public boolean editNode(final BasicStrutsNode node) {
    if (node == null) { // TODO should not happen
      return false;
    }

    final XmlElement xmlElement = node.getIdentifyingElement().getXmlElement();
    if (xmlElement != null && xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable) xmlElement}, true);
      return true;
    }
    return super.editNode(node);
  }

  public boolean editEdge(final BasicStrutsEdge edge) {
    if (edge == null) {
      return false; // TODO should not happen
    }

    final XmlElement xmlElement = edge.getSource().getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable) xmlElement}, true);
      return true;
    }
    return super.editEdge(edge);
  }

  public String getNodeTooltip(@Nullable final BasicStrutsNode node) {
    if (node == null) {
      return null;
    }

    return ElementPresentationManager.getDocumentationForElement(node.getIdentifyingElement());
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowBendCreation(false);
    editMode.allowEdgeCreation(false);

    view.setFitContentOnResize(false);
    view.setAntialiasedPainting(false);
    view.setGridVisible(false);
    view.fitContent();
  }

}