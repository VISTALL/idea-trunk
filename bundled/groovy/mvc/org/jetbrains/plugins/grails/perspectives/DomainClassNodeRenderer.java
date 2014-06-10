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

package org.jetbrains.plugins.grails.perspectives;

import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.builder.renderer.GradientFilledPanel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2DSelectionEvent;
import com.intellij.openapi.graph.view.Graph2DSelectionListener;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.graph.view.ViewChangeListener;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.perspectives.graph.DataModelAndSelectionModificationTracker;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassNode;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassesRelationsDataModel;
import org.jetbrains.plugins.groovy.GroovyIcons;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 08.08.2007
 */
public class DomainClassNodeRenderer extends AbstractColoredNodeCellRenderer {
  private final GraphBuilder<DomainClassNode, DomainClassRelationsInfo> myBuilder;
  private final DomainClassesRelationsDataModel myDataModel;
  private final Color myBackgroundColor = new Color(252, 250, 209);
  private final Color myCaptionBackgroundColor = new Color(215, 213, 172);  //new Color(186, 222, 193)

  private java.util.List<Edge> selectedEdges = new ArrayList<Edge>();
  ViewChangeListener myViewChangeListener;

  public DomainClassNodeRenderer(@NotNull GraphBuilder<DomainClassNode, DomainClassRelationsInfo> builder, DataModelAndSelectionModificationTracker modificationTracker, DomainClassesRelationsDataModel dataModel) {
    super(modificationTracker);
    myBuilder = builder;
    myDataModel = dataModel;

    myBuilder.getView().getGraph2D().addGraph2DSelectionListener(new Graph2DSelectionListener() {
      public void onGraph2DSelectionEvent(Graph2DSelectionEvent event) {
        if (event.isEdgeSelection()) {
          selectedEdges = GraphViewUtil.getSelectedEdges(event.getGraph2D());
        }
      }
    });

     myBuilder.getView().getGraph2D().addGraph2DSelectionListener(modificationTracker);
  }

  public void tuneNode(NodeRealizer realizer, JPanel wrapper) {
    wrapper.removeAll();

    final Icon entityIcon = GroovyIcons.CLASS;
    final Icon varNameIcon = GroovyIcons.PROPERTY;

    Node node = realizer.getNode();
    DomainClassNode domainClassNode = myBuilder.getNodeObject(node);
    JLabel nodeNameLabel = new JLabel(myBuilder.getNodeName(domainClassNode), entityIcon, JLabel.HORIZONTAL);
    nodeNameLabel.setBorder(IdeBorderFactory.createEmptyBorder(3, 3, 3, 3));
    nodeNameLabel.setHorizontalAlignment(SwingConstants.LEFT);

    GradientFilledPanel namePanel = new GradientFilledPanel(myCaptionBackgroundColor);
    namePanel.setLayout(new BorderLayout());
    namePanel.add(nodeNameLabel, BorderLayout.CENTER);
    namePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

    namePanel.setGradientColor(new Color(186, 222, 193));

    nodeNameLabel.setForeground(Color.BLACK);
    wrapper.add(namePanel, BorderLayout.NORTH);

    assert domainClassNode != null;

    final List<DomainClassRelationsInfo> outEdges = myDataModel.getNodesToOutsMap().get(domainClassNode);

    if (outEdges != null) {
      Iterator<DomainClassRelationsInfo> iterator = outEdges.iterator();

      final JPanel relationsPanel = new JPanel(new GridBagLayout());
      relationsPanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 5, 2, 5));
      relationsPanel.setBackground(myBackgroundColor);

      boolean isBold;
      if (!outEdges.isEmpty()) {
        while (iterator.hasNext()) {
          DomainClassRelationsInfo edge = iterator.next();
          isBold = selectedEdges.contains(myBuilder.getEdge(edge));

          final String varName = edge.getVarName();
          final JLabel varNamePanel = new JLabel(varName);
          if (isBold) {
            varNamePanel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
          }

          varNamePanel.setIcon(varNameIcon);

          final String type = edge.getTarget().getTypeDefinition().getName();

          final JLabel typeLabel = new JLabel(type);
          if (isBold) {
            typeLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
          }

          final JPanel typePanel = new JPanel(new BorderLayout());
          typePanel.add(typeLabel, BorderLayout.EAST);
          typePanel.setOpaque(false);

          relationsPanel.add(varNamePanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
              GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
              new Insets(2, 2, 2, 2), 0, 0));
          relationsPanel.add(typePanel, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.LINE_END,
              GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        }

        Dimension preferredSize = relationsPanel.getPreferredSize();
        relationsPanel.setPreferredSize(new Dimension((int) preferredSize.getWidth() + 20, (int) preferredSize.getHeight()));

      } else {
        Dimension preferredSize = nodeNameLabel.getPreferredSize();
        nodeNameLabel.setPreferredSize(new Dimension((int) preferredSize.getWidth() + 25, (int) preferredSize.getHeight()));
      }

      wrapper.add(relationsPanel);
    }
  }
}
