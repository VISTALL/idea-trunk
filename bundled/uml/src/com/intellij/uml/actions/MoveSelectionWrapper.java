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

package com.intellij.uml.actions;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.NodeLayout;
import com.intellij.openapi.graph.view.Graph2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class MoveSelectionWrapper extends AbstractAction {
  private final Graph2D myGraph;
  private final Keys myKey;

  public static enum Keys {UP, DOWN, LEFT, RIGHT}

  public MoveSelectionWrapper(Keys key, Graph2D graph) {
    myGraph = graph;
    myKey = key;
  }

  public void actionPerformed(final ActionEvent e) {
    final List<Node> selectedNodes = GraphViewUtil.getSelectedNodes(myGraph);
    final Node node;
    if (selectedNodes.size() == 0) {
      final Node[] nodes = myGraph.getNodeArray();
      node =  nodes.length > 0 ? nodes[0] : null;
    } else {
      node = selectedNodes.get(0);
    }
    if (node == null) return;

    myGraph.setSelected(node, false);
    final Node toSelect = findNearestNode(node);
    myGraph.setSelected(toSelect, true);
    myGraph.updateViews();
  }

  private Node findNearestNode(Node node) {
    Node n;
    double angle = Math.PI / 3;
    do {
      List<Node> nodes = new ArrayList<Node>(Arrays.asList(myGraph.getNodeArray()));
      nodes.remove(node);      
      nodes = filterNodes(node, nodes, myKey, angle);
      n = findNearest(node, nodes);
      angle += Math.PI / 3;
    } while (n == node && angle <= Math.PI);
    return n;
  }

  private static Node findNearest(Node node, List<Node> nodes) {
    Node best = node;
    final Point2D.Double coord = getNodeCoord(node);
    double distance = Double.MAX_VALUE;
    for (Node n : nodes) {
      final double dist = coord.distance(getCoordOfNearestCorner(n, coord));
      if (distance > dist) {
        best = n;
        distance = dist;
      }
    }
    return best;
  }

  private static List<Node> filterNodes(Node node, List<Node> nodes, Keys key, double angle) {
    List<Node> filtered = new ArrayList<Node>();    
    for (Node n : nodes) {
      final double alpha = calculateAngle(node, n, key);
      if (2*Math.abs(alpha) <= angle) {
        filtered.add(n);
      }
    }
    return filtered;
  }

  private static double calculateAngle(Node center, Node node, Keys key) {
    final Point2D.Double a = getNodeCoord(center);
    final Point2D.Double b = getCoordOfNearestCorner(node, a);
    int y = key == Keys.UP ? -1 : key == Keys.DOWN ? 1 : 0;
    int x = key == Keys.LEFT ? -1 : key == Keys.RIGHT ? 1 : 0;
    final Point2D.Double c = new Point2D.Double(a.getX() + x, a.getY() + y);
    final double A = b.distance(c);
    final double B = a.distance(c);
    final double C = a.distance(b);
    // A^2 = B^2 + C^2 - 2*B*C*cos(alpha)
    return Math.acos((B*B + C*C - A*A) / (2 * B * C));
  }

  private static Point.Double getNodeCoord(Node node) {
    final NodeLayout layout = ((Graph2D)node.getGraph()).getNodeLayout(node);
    return new Point2D.Double(layout.getX() + layout.getWidth() / 2, layout.getY() + layout.getHeight() / 2);
  }

  private static Point.Double getCoordOfNearestCorner(Node node, Point.Double center) {
    final NodeLayout layout = ((Graph2D)node.getGraph()).getNodeLayout(node);
    final double x = layout.getX();
    final double y = layout.getY();
    final double w = layout.getWidth();
    final double h = layout.getHeight();
    Point.Double best = new Point2D.Double(x, y);
    double dist = center.distance(x,y);
    if (dist > center.distance(x+w,y)) {
      best = new Point2D.Double(x+w, y);
      dist = center.distance(best);
    }
    if (dist > center.distance(x, y+h)) {
      best = new Point2D.Double(x, y+h);
      dist = center.distance(best);
    }
    if (dist > center.distance(x+w, y+h)) {
      best = new Point2D.Double(x+w, y+h);
      dist = center.distance(best);
    }
    if (dist > center.distance(x + w/2, y + h/2)) {
      best = new Point2D.Double(x + w/2, y + h/2);
    }
    return best;
  }
}