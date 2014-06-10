/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.perspectives.graph;

import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Graph;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.perspectives.DomainClassNodeRenderer;
import org.jetbrains.plugins.grails.perspectives.delete.RelationDeleteProvider;

import java.awt.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassDependencyPresentation extends BasicGraphPresentationModel<DomainClassNode, DomainClassRelationsInfo> {
  private final DomainClassesRelationsDataModel myDataModel;
  private DomainClassNodeRenderer myNodeCellRenderer;

  public DomainClassDependencyPresentation(Graph graph, DomainClassesRelationsDataModel dataModel) {
    super(graph);
    myDataModel = dataModel;

    GraphSettings graphSettings = GraphSettingsProvider.getInstance(myDataModel.getProject()).getSettings(graph);
    graphSettings.setCurrentLayouter(graphSettings.getOrthogonalLayouter());

    GraphSettingsProvider.getInstance(myDataModel.getProject()).getSettings(graph).setFitContentAfterLayout(true);

    setShowEdgeLabels(true);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final DomainClassNode node) {
    if (myNodeCellRenderer == null) {
      GraphBuilder<DomainClassNode, DomainClassRelationsInfo> builder = getGraphBuilder();
      DataModelAndSelectionModificationTracker tracker = new DataModelAndSelectionModificationTracker(myDataModel.getProject());
      myNodeCellRenderer = new DomainClassNodeRenderer(builder, tracker, myDataModel);
    }
    return GraphViewUtil.createNodeRealizer("DomainClassNodeRenderer", myNodeCellRenderer);
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(DomainClassRelationsInfo edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();
    edgeRealizer.setLineType(LineType.LINE_1);

    switch (edge.getRelation()) {
      case UNKNOWN: {
        edgeRealizer.setLineColor(Color.GREEN.darker());
        edgeRealizer.setTargetArrow(Arrow.STANDARD);
        edgeRealizer.setSourceArrow(Arrow.NONE);
        break;
      }

      case STRONG: {
        edgeRealizer.setLineColor(Color.CYAN.darker());
        edgeRealizer.setTargetArrow(Arrow.STANDARD);
        edgeRealizer.setSourceArrow(Arrow.NONE);
        break;
      }

      case BELONGS_TO: {
        edgeRealizer.setLineColor(Color.GREEN.darker());
        edgeRealizer.setTargetArrow(Arrow.STANDARD);
        edgeRealizer.setSourceArrow(Arrow.NONE);

        break;
      }

      case HAS_MANY: {
        edgeRealizer.setLineColor(Color.BLUE.darker());
        edgeRealizer.setTargetArrow(Arrow.STANDARD);
        edgeRealizer.setSourceArrow(Arrow.NONE);
        break;
      }

      default: {
        edgeRealizer.setLineType(LineType.LINE_1);
        edgeRealizer.setLineColor(Color.GRAY);
        edgeRealizer.setArrow(Arrow.STANDARD);

        break;
      }
    }

    return edgeRealizer;
  }

  public String getEdgeTooltip(final DomainClassRelationsInfo edge) {
    return edge.getEdgeLabel();
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowEdgeCreation(true);
    editMode.allowBendCreation(false);
    view.setFitContentOnResize(true);
  }

  public boolean editNode(DomainClassNode domainClassNode) {
    domainClassNode.getTypeDefinition().navigate(true);
    return true;
  }

  public DeleteProvider getDeleteProvider() {
    return new RelationDeleteProvider(myDataModel.getProject());
  }
}
